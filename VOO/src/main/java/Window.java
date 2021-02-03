import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Window extends Component {
    public final String componentType = "Window";

    public String name;
    public File file;
    public Tab tab;
    public Stage stage;
    public List<Class> classes = new ArrayList<>();
    public List<Component> windowLayers = new ArrayList<>();
    public List<Pair<TreeItem<String>, Component>> treeItemPairs = new ArrayList<>();

    public Window(String name, Stage stage){
        if(name.equals("")) name = "<untitled>";
        this.name = name;
        windowLayers.add(this);
        allComponents = new ArrayList<>();
        this.stage = stage;
    }
    public Window(Stage stage){
        this("<untitled>", stage);
    }
    public Window(Stage stage, File file){
        this.stage = stage;
        try {
            String data_file = new Scanner(file).nextLine();
            JSONObject json = new JSONObject(data_file);
            allComponents = new ArrayList<>();
            this.file = file;
            windowLayers.add(this);
            this.name = json.getString("name");
            for (Object subJson: json.getJSONArray("classes")){ this.addClass(new Class((JSONObject) subJson, this)); }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getCompType(){ return this.componentType; }
    public String getNameView(){ return this.name; }
    public String getName(){ return this.name; }

    public void addClass(Class class_){
        this.classes.add(class_);
        this.allComponents.add(class_);
    }
    public void removeClass(Class class_){
        this.classes.remove(class_);
        this.allComponents.remove(class_);
    }

    public void pushWindowLayers(Component window){ windowLayers.add(window); }
    public void popWindowLayers(){ windowLayers.remove(windowLayers.size()-1); }

    public String toJava(String indentation) {
        String text = "";
        if (this.classes.size() != 0) {
            for (Class cls : this.classes)
                text += "\n\n" + cls.toJava("");
        }
        return text;
    }

    public JSONObject toJson(){
        JSONObject items = new JSONObject();

        items.put("name", this.getName());
        items.put("component-type", "Window");

        JSONArray classesArrayJson = new JSONArray();
        for(Class class_: this.classes){ classesArrayJson.put(class_.toJson()); }
        items.put("classes", classesArrayJson);

        return items;
    }

    public void saveToJava(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("java files (*.java)", "*.java");
        fileChooser.getExtensionFilters().add(extFilter);
        File javaFile = fileChooser.showSaveDialog(stage);
        String content = this.toJava("");
        saveTextToFile(content, javaFile);
    }

    public Boolean makeFile(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("VOO files (*.voo)", "*.voo");
        fileChooser.getExtensionFilters().add(extFilter);
        File vooFile = fileChooser.showSaveDialog(stage);
        if (vooFile != null) {
            this.file = vooFile;
            String namWithFormat = this.file.getName();
            this.name = namWithFormat.substring(0, namWithFormat.length()-4);
            return save();
        }
        return false;
    }

    public void saveAs(){
        if(save() && makeFile()) save();
    }

    public Boolean save(){
        return saveTextToFile(toJson().toString(), this.file);
    }

    private Boolean saveTextToFile(String content, File file) {
        if(file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                writer.println(content);
                writer.close();
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    public void showAsWindow(AnchorPane group){
        double extra_x = 40.0, extra_x1;
        for(Class class_: classes) {
            AnchorPane groupC = new AnchorPane();
            extra_x1 = class_.showAsBox(groupC);
            AnchorPane.setLeftAnchor(groupC, extra_x);
            AnchorPane.setTopAnchor(groupC, 50.0);
            group.getChildren().add(groupC);
            extra_x += extra_x1 + 20;
        }
    }

    public void detailsShow(AnchorPane group){
        double fontSize = 15;

        Label componentLabel = new Label("Main Window");
        componentLabel.setFont(Font.font(fontSize));

        Label nameLabel = new Label("Name: " + this.name);
        nameLabel.setFont(Font.font(fontSize));

        String classesString = "Classes:";
        if(this.classes.size() == 0){
            classesString += "\n    " + "-------";
        }else {
            for (Class cls : this.classes) { classesString += "\n    " + cls.getName(); }
        }
        Label classesLabel = new Label(classesString);
        classesLabel.setFont(Font.font(fontSize));

        VBox box = new VBox(5);
        box.getChildren().addAll(componentLabel, nameLabel, classesLabel);
        group.getChildren().addAll(box);
    }

    public String compoIcon(Component component){
        String compType = component.getCompType();
        if("Window".equals(compType)) return "window.png";
        else if("Class".equals(compType)){
            Class cls = (Class) component;
            if(cls.type.equals("class")) return "class.png";
            if(cls.type.equals("interface")) return "interface.png";
            if(cls.type.equals("enum")) return "enum.png";
        }
        else if("Function".equals(compType)) return "function.png";
        else if("Variable".equals(compType)) {
            Variable var = (Variable) component;
            if ("Class".equals(var.parent.getCompType())) return "attribute.png";
            if ("Function".equals(var.parent.getCompType())) return "parameter.png";
        }
        return "";
    }

    public TreeItem<String> treeItems(Component component){
        TreeItem<String> items = null;
        try {
            ImageView imgIcon = new ImageView(new Image(new FileInputStream(new File("src\\main\\resources\\"+compoIcon(component)))));
            imgIcon.setFitHeight(16);
            imgIcon.setFitWidth(16);
            imgIcon.setPreserveRatio(true); imgIcon.setSmooth(true); imgIcon.setCache(true);
            items = new TreeItem<>(component.getNameView(), imgIcon);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        treeItemPairs.add(new Pair<>(items, component));
        items.setExpanded(true);
        String compType = component.getCompType();
        if ("Window".equals(compType) || "Class".equals(compType) || "Function".equals(compType)) {
            if(!Objects.isNull(component.allComponents) && component.allComponents.size() != 0) {
                for (Component subComponent : component.allComponents) {
                    items.getChildren().add(treeItems(subComponent));
                }
            }
        }
        return items;
    }

    public TreeView<String> treeViewShow(){
        TreeView<String> tree = new TreeView<>(treeItems(this));
        tree.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                TreeItem<String> item = tree.getSelectionModel().getSelectedItem();
                Component selectedComponent = findTreeItemPair(item);
                if(selectedComponent != null) {
                    pushWindowLayers(selectedComponent);
                    Screen.setTabsBox();
                }
            }
        });

        tree.setMinWidth(260);
        tree.setPrefHeight(365);
        return tree;
    }

    public Component findTreeItemPair(TreeItem<String> treeItem){
        if(treeItemPairs != null && treeItemPairs.size() != 0) {
            for (Pair<TreeItem<String>, Component> item : treeItemPairs) {
                if (item.getKey().equals(treeItem)) {
                    return item.getValue();
                }
            }
        }
        return null;
    }

    public void setTab(){
        Component lastLayer = windowLayers.get(windowLayers.size()-1);
        Tab tab = new Tab();
        tab.setText(this.getName());

        ScrollPane mainScroll = new ScrollPane();
        AnchorPane mainGroup = new AnchorPane();
        AnchorPane groupS = new AnchorPane();
        lastLayer.showAsWindow(groupS);

        mainGroup.getChildren().add(groupS);
        mainScroll.setContent(mainGroup);
        mainScroll.setPadding(new Insets(10));
        mainScroll.setPrefSize(1300,830);

        Label treeViewLabel = new Label("Components View");
        treeViewLabel.setFont(Font.font(20));
        AnchorPane.setLeftAnchor(treeViewLabel, 1370.0);

        ScrollPane treeViewScroll = new ScrollPane();
        AnchorPane treeViewGroup = new AnchorPane();
        TreeView<String> tree = this.treeViewShow();
        treeViewGroup.getChildren().add(tree);
        treeViewScroll.setContent(treeViewGroup);
        treeViewScroll.setPadding(new Insets(10));
        treeViewScroll.setPrefSize(300,385);
        AnchorPane.setLeftAnchor(treeViewScroll, 1300.0);
        AnchorPane.setTopAnchor(treeViewScroll, 30.0);

        Label detailsLabel = new Label("Details");
        detailsLabel.setFont(Font.font(20));
        AnchorPane.setLeftAnchor(detailsLabel, 1420.0);
        AnchorPane.setTopAnchor(detailsLabel, 415.0);

        ScrollPane detailsScroll = new ScrollPane();
        AnchorPane detailsGroup = new AnchorPane();
        AnchorPane groupD = new AnchorPane();
        lastLayer.detailsShow(groupD);
        detailsGroup.getChildren().add(groupD);
        detailsScroll.setContent(detailsGroup);
        detailsScroll.setPadding(new Insets(10));
        detailsScroll.setPrefSize(300,385);
        AnchorPane.setLeftAnchor(detailsScroll, 1300.0);
        AnchorPane.setTopAnchor(detailsScroll, 445.0);

        AnchorPane allGroup = new AnchorPane();
        allGroup.getChildren().addAll(mainScroll, treeViewLabel, treeViewScroll, detailsLabel, detailsScroll);

        tab.setContent(allGroup);

        this.tab = tab;
    }
}