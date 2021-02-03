import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.controlsfx.control.CheckComboBox;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class Screen {
    public static List<Window> windows =  new ArrayList<>();;
    public static TabPane tabsBox;
    public BorderPane root;
    public Scene scene;
    public MenuBar menuBar;
    public VBox topContainer;
    public Stage stage;

    public Screen(Stage stage){
        tabsBox = new TabPane();
        this.root = new BorderPane();
        double width = 1600;
        double height = 900;
        this.scene = new Scene(root, width, height);
        this.menuBar = new MenuBar();
        this.topContainer = new VBox();

        this.stage = stage;
        this.stage.setTitle("VOO");
        this.stage.setResizable(false);
        try {
            String vooIconPath = "src\\main\\resources\\appIcon.png";
            this.stage.getIcons().add(new Image(new FileInputStream(vooIconPath)));
        } catch (FileNotFoundException ignored) { }
        this.setMenuBar();
    }

    public void run(){
        setTabsBox();
        topContainer.getChildren().addAll(menuBar, tabsBox);
        root.setTop(topContainer);
        stage.setScene(scene);
        stage.show();
    }

    public static Window currentWindow(){
        Tab tab = tabsBox.getSelectionModel().getSelectedItem();
        for(Window window: windows)
            if (window.tab == tab) return window;
        return null;
    }

    public void closeWindow(Window window){
        window.save();
        tabsBox.getTabs().remove(window.tab);
        windows.remove(window);
    }
    public void closeAllWindows(){
        if(windows != null && windows.size() != 0) {
            Iterator<Window> interW = windows.iterator();
            while (interW.hasNext()) closeWindow(interW.next());
        }
    }

    public void closeCurrentWindow(){
        if(windows != null && windows.size() != 0) closeWindow(currentWindow());
    }

    public void saveAllWindows(){
        if(windows != null && windows.size() != 0){
            Iterator<Window> interW = windows.iterator();
            while (interW.hasNext()) interW.next().save();
        }
    }

    public void saveCurrentWindow(){
        if(windows != null && windows.size() != 0) currentWindow().save();
    }

    public void saveAsCurrentWindow(){
        if(windows != null && windows.size() != 0) currentWindow().saveAs();
    }

    public void saveToJava(){
        if(windows != null && windows.size() != 0) currentWindow().saveToJava();
    }

    public void exit(){
        saveAllWindows();
        stage.close();
    }

    public void newWindow(){
        Window window = new Window(stage);
        if(window.makeFile()){
            windows.add(window);
            window.setTab();
            tabsBox.getTabs().add(window.tab);
            window.tab.setOnClosed(event -> closeWindow(window));
        }
    }

    public void openWindow(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("VOO files (*.voo)", "*.voo");
        fileChooser.getExtensionFilters().add(extFilter);
        File tempFile = fileChooser.showOpenDialog(stage);
        if (tempFile != null) {
            Window window = new Window(stage, tempFile);
            windows.add(window);
            window.setTab();
            tabsBox.getTabs().add(window.tab);
            window.tab.setOnClosed(event -> closeWindow(window));
        }
    }

    public static class ClassResult{
        String name;
        String type;
        String privileges;
        String parents;
        List<String> statuses;

        ClassResult(String name, String type, String privileges, String parents, ObservableList<String> statuses){
            this.name = name;
            this.type = type;
            this.privileges = privileges;
            this.parents = parents;
            this.statuses = new ArrayList<>(statuses);
        }
    }

    public void add_edit_ClassToCurrentWindow(ClassResult result, Component lastLayer, Boolean asEdit){
        if(result != null) {
            Class cls;
            if (asEdit){
                cls = (Class) lastLayer;
                cls.name = result.name;
                cls.type = result.type;
                cls.statuses = result.statuses;
                cls.privilege = result.privileges;
                cls.parents = Arrays.asList(result.parents.split(","));
            }

            else {
                cls = new Class(result.name, result.type, lastLayer);
                cls.statuses = result.statuses;
                cls.privilege = result.privileges;
                if (lastLayer.getCompType().equals("Class")) {
                    Class lastL = (Class) lastLayer;
                    lastL.addClass(cls);
                } else if (lastLayer.getCompType().equals("Window")) {
                    Window lastL = (Window) lastLayer;
                    lastL.addClass(cls);
                }
                cls.parents = Arrays.asList(result.parents.split(","));
            }
            setTabsBox();
        }
    }

    public void new_edit_ClassDialog(Component lastLayer, String title, String header, Boolean asEdit){
        Dialog<ClassResult> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Stage tempStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        try {
            tempStage.getIcons().add(new Image(new FileInputStream(new File("src\\main\\resources\\class.png"))));
        } catch (FileNotFoundException ignored) { }
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setMinWidth(300);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label nameLabel = new Label("Class Name:");
        TextField nameTextField = new TextField();

        Label privilegesLabel = new Label("Privileges:");
        ObservableList<String> privilegesOptions = FXCollections.observableArrayList();
        privilegesOptions.addAll("public", "private", "protected");
        ComboBox<String> privilegesComboBox = new ComboBox<>(privilegesOptions);
        privilegesComboBox.getSelectionModel().selectFirst();

        Label statusesLabel = new Label("Statuses:");
        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll("static", "abstract", "const", "virtual");
        CheckComboBox<String> StatusesComboBox = new CheckComboBox<>(items);

        Label typesLabel = new Label("Type:");
        ObservableList<String> typesOptions = FXCollections.observableArrayList();
        typesOptions.addAll("class", "interface", "enum");
        ComboBox<String> typesComboBox = new ComboBox<>(typesOptions);
        typesComboBox.getSelectionModel().selectFirst();

        Label parentsLabel = new Label("Parents(separate with comma):");
        TextField parentsTextField = new TextField();

        if(asEdit) {
            Class cls = (Class) lastLayer;

            nameTextField.setText(cls.name);

            privilegesComboBox.getSelectionModel().select(cls.privilege);

            if(cls.statuses != null && cls.statuses.size() != 0){
                for(String status: cls.statuses)
                    StatusesComboBox.getCheckModel().check(status);
            }

            typesComboBox.getSelectionModel().select(cls.type);

            String parentsText = "";
            if(cls.parents != null && cls.parents.size() != 0) {
                parentsText += cls.parents.get(0);
                for (String parent : cls.parents.subList(1, cls.parents.size())) parentsText += "," + parent;
            }
            parentsTextField.setText(parentsText);
        }

        dialogPane.setContent(new VBox(8,
                nameLabel, nameTextField,
                privilegesLabel, privilegesComboBox,
                statusesLabel, StatusesComboBox,
                typesLabel, typesComboBox,
                parentsLabel, parentsTextField));

        Platform.runLater(nameTextField::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK)
                return new ClassResult(nameTextField.getText(), typesComboBox.getValue(), privilegesComboBox.getValue(), parentsTextField.getText(), StatusesComboBox.getCheckModel().getCheckedItems());
            return null;
        });
        Optional<ClassResult> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent(result -> add_edit_ClassToCurrentWindow(result, lastLayer, asEdit));
    }

    public void makeNewClass(){
        if(windows.size() != 0) {
            Window cWindow = currentWindow();
            if(cWindow != null) {
                Component lastLayer = cWindow.windowLayers.get(cWindow.windowLayers.size() - 1);
                if (lastLayer.getCompType().equals("Window") || lastLayer.getCompType().equals("Class")) {
                    new_edit_ClassDialog(lastLayer, "New Class", "Define the properties of the new Class", false);
                }
            }
        }
    }

    public static class FunctionResult{
        String name;
        String returnType;
        String privileges;
        String body;
        List<String> statuses;

        FunctionResult(String name, String returnType, String privileges, String body, ObservableList<String> statuses){
            this.name = name;
            this.returnType = returnType;
            this.privileges = privileges;
            this.body = body;
            this.statuses = new ArrayList<>(statuses);
        }
    }

    public void add_edit_FunctionToCurrentWindow(FunctionResult result, Component lastLayer, Boolean asEdit){
        if(result != null) {
            if(asEdit){
                Function func = (Function) lastLayer;
                func.name = result.name;
                func.returnType = result.returnType;
                func.statuses = result.statuses;
                func.privilege = result.privileges;
                func.body = result.body;
            }

            else {
                Function func = new Function(result.name, result.returnType, lastLayer);
                func.statuses = result.statuses;
                func.privilege = result.privileges;
                func.body = result.body;
                Class lastL = (Class) lastLayer;
                lastL.addMethod(func);
            }
            setTabsBox();
        }
    }

    public void new_edit_FunctionDialog(Component lastLayer, String title, String header, Boolean asEdit){
        Dialog<FunctionResult> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Stage tempStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        try {
            tempStage.getIcons().add(new Image(new FileInputStream(new File("src\\main\\resources\\function.png"))));
        } catch (FileNotFoundException ignored) { }
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setMinWidth(300);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label nameLabel = new Label("Method Name:");
        TextField nameTextField = new TextField();

        Label returnTypeLabel = new Label("Return Type:");
        TextField returnTypeTextField = new TextField();

        Label privilegesLabel = new Label("Privileges:");
        ObservableList<String> privilegesOptions = FXCollections.observableArrayList();
        privilegesOptions.addAll("public", "private", "protected");
        ComboBox<String> privilegesComboBox = new ComboBox<>(privilegesOptions);
        privilegesComboBox.getSelectionModel().selectFirst();

        Label statusesLabel = new Label("Statuses:");
        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll("static", "abstract", "const", "virtual");
        CheckComboBox<String> StatusesComboBox = new CheckComboBox<>(items);

        Label bodyLabel = new Label("Body:");
        TextArea bodyTextArea = new TextArea();

        if(asEdit) {
            Function func = (Function) lastLayer;

            nameTextField.setText(func.name);

            privilegesComboBox.getSelectionModel().select(func.privilege);

            if(func.statuses != null && func.statuses.size() != 0){
                for(String status: func.statuses)
                    StatusesComboBox.getCheckModel().check(status);
            }

            returnTypeTextField.setText(func.returnType);

            bodyTextArea.setText(func.body);
        }

        dialogPane.setContent(new VBox(8,
                nameLabel, nameTextField,
                returnTypeLabel, returnTypeTextField,
                privilegesLabel, privilegesComboBox,
                statusesLabel, StatusesComboBox,
                bodyLabel, bodyTextArea));

        Platform.runLater(nameTextField::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK)
                return new FunctionResult(nameTextField.getText(), returnTypeTextField.getText(), privilegesComboBox.getValue(), bodyTextArea.getText(), StatusesComboBox.getCheckModel().getCheckedItems());
            return null;
        });
        Optional<FunctionResult> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent(result -> add_edit_FunctionToCurrentWindow(result, lastLayer, asEdit));
    }

    public void makeNewFunction(){
        if(windows.size() != 0) {
            Window cWindow = currentWindow();
            if(cWindow != null) {
                Component lastLayer = cWindow.windowLayers.get(cWindow.windowLayers.size() - 1);
                if (lastLayer.getCompType().equals("Class")) {
                    new_edit_FunctionDialog(lastLayer, "New Method", "Define the properties of the new Method", false);
                }
            }
        }
    }

    public static class VariableResult{
        String name;
        String type;
        String privileges;
        List<String> statuses;

        VariableResult(String name, String type, String privileges, ObservableList<String> statuses){
            this.name = name;
            this.type = type;
            this.privileges = privileges;
            this.statuses = new ArrayList<>(statuses);
        }
    }

    public void add_edit_VariableToCurrentWindow(VariableResult result, Component lastLayer, Boolean asEdit){
        if(result != null) {
            if(asEdit){
                Variable var = (Variable) lastLayer;
                var.name = result.name;
                var.type = result.type;
                var.statuses = result.statuses;
                var.privilege = result.privileges;
            }

            else {
                Variable var = new Variable(result.name, result.type, lastLayer);
                var.statuses = result.statuses;
                var.privilege = result.privileges;
                if (lastLayer.getCompType().equals("Class")) {
                    Class lastL = (Class) lastLayer;
                    lastL.addAttribute(var);
                } else if (lastLayer.getCompType().equals("Function")) {
                    Function lastL = (Function) lastLayer;
                    lastL.addParameter(var);
                }
            }
            setTabsBox();
        }
    }

    public void new_edit_VariableDialog(String asWhat, Component lastLayer, String title, String header, Boolean asEdit){
        Dialog<VariableResult> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Stage tempStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        try {
            tempStage.getIcons().add(new Image(new FileInputStream(new File("src\\main\\resources\\" + asWhat.toLowerCase() + ".png"))));
        } catch (FileNotFoundException ignored) { }
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setMinWidth(300);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label nameLabel = new Label(asWhat + " Name:");
        TextField nameTextField = new TextField();

        Label typeLabel = new Label("Type:");
        TextField typeTextField = new TextField();

        Label privilegesLabel = new Label("Privileges:");
        ObservableList<String> privilegesOptions = FXCollections.observableArrayList();
        privilegesOptions.addAll("public", "private", "protected");
        ComboBox<String> privilegesComboBox = new ComboBox<>(privilegesOptions);
        privilegesComboBox.getSelectionModel().selectFirst();

        Label statusesLabel = new Label("Statuses:");
        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll("static", "abstract", "const", "virtual");
        CheckComboBox<String> StatusesComboBox = new CheckComboBox<>(items);

        if(asEdit) {
            Variable var = (Variable) lastLayer;

            nameTextField.setText(var.name);

            typeTextField.setText(var.type);

            privilegesComboBox.getSelectionModel().select(var.privilege);

            if(var.statuses != null && var.statuses.size() != 0){
                for(String status: var.statuses)
                    StatusesComboBox.getCheckModel().check(status);
            }
        }

        dialogPane.setContent(new VBox(8,
                nameLabel, nameTextField,
                typeLabel, typeTextField,
                privilegesLabel, privilegesComboBox,
                statusesLabel, StatusesComboBox));

        Platform.runLater(nameTextField::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK)
                return new VariableResult(nameTextField.getText(), typeTextField.getText(), privilegesComboBox.getValue(), StatusesComboBox.getCheckModel().getCheckedItems());
            return null;
        });
        Optional<VariableResult> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent(result -> add_edit_VariableToCurrentWindow(result, lastLayer, asEdit));
    }

    public void makeNewVariable(){
        if(windows.size() != 0) {
            Window cWindow = currentWindow();
            if(cWindow != null) {
                Component lastLayer = cWindow.windowLayers.get(cWindow.windowLayers.size() - 1);
                String lastLayerCompoType = lastLayer.getCompType();
                if (lastLayerCompoType.equals("Class")) {
                    new_edit_VariableDialog("Attribute", lastLayer, "New Attribute", "Define the properties of the new Attribute", false);
                } else if (lastLayerCompoType.equals("Function")) {
                    new_edit_VariableDialog("Parameter", lastLayer, "New Parameter", "Define the properties of the new Parameter", false);
                }
            }
        }
    }

    public void removeLastLayerCurrentWindow(){
        if(windows.size() != 0) {
            Window window = currentWindow();
            if(window != null && window.windowLayers.size() > 1) {
                window.popWindowLayers();
                setTabsBox();
            }
        }
    }

    public void editLastLayerCurrentWindow(){
        if(windows.size() != 0) {
            Window window = currentWindow();
            if(window != null && window.windowLayers.size() > 1) {
                Component lastLayer = window.windowLayers.get(window.windowLayers.size() - 1);
                String lastLayerCompoType = lastLayer.getCompType();
                if(lastLayerCompoType.equals("Class")){
                    new_edit_ClassDialog(lastLayer, "Edit Class", "Edit the properties of the Class", true);
                }
                else if(lastLayerCompoType.equals("Function")){
                    new_edit_FunctionDialog(lastLayer, "Edit Method", "Edit the properties of the Method", true);
                }
                else if(lastLayerCompoType.equals("Variable")){
                    Variable var = (Variable) lastLayer;
                    if("Class".equals(var.parent.getCompType()))
                        new_edit_VariableDialog("Attribute", lastLayer, "Edit Attribute", "Edit the properties of the Attribute", true);
                    else
                        new_edit_VariableDialog("Parameter", lastLayer, "Edit Parameter", "Edit the properties of the Parameter", true);
                }
                setTabsBox();
            }
        }
    }

    public void duplicateLastLayerCurrentWindow(){
        if(windows.size() != 0) {
            Window window = currentWindow();
            if (window != null && window.windowLayers.size() > 1) {
                Component lastLayer = window.windowLayers.get(window.windowLayers.size() - 1);
                String lastLayerCompoType = lastLayer.getCompType();
                if (lastLayerCompoType.equals("Class")) {
                    Class cls = (Class) lastLayer;
                    if("Window".equals(cls.parent.getCompType())){
                        Window parentW = (Window) cls.parent;
                        parentW.addClass(new Class(cls));
                    }else{
                        Class parentC = (Class) cls.parent;
                        parentC.addClass(new Class(cls));
                    }
                } else if (lastLayerCompoType.equals("Function")) {
                    Function func = (Function) lastLayer;
                    Class parentC = (Class) func.parent;
                    parentC.addMethod(new Function(func));
                } else if (lastLayerCompoType.equals("Variable")) {
                    Variable var = (Variable) lastLayer;
                    if("Window".equals(var.parent.getCompType())){
                        Class parentC = (Class) var.parent;
                        parentC.addAttribute(new Variable(var));
                    }else{
                        Function parentF = (Function) var.parent;
                        parentF.addParameter(new Variable(var));
                    }
                }
                setTabsBox();
            }
        }
    }

    public void deleteLastLayerCurrentWindow(){
        if(windows.size() != 0) {
            Window window = currentWindow();
            if (window != null && window.windowLayers.size() > 1) {
                Component lastLayer = window.windowLayers.get(window.windowLayers.size() - 1);
                String lsatLayerCompoType = lastLayer.getCompType();
                if("Class".equals(lsatLayerCompoType)){
                    Class layer = (Class) lastLayer;
                    Component parent = layer.parent;
                    String parentCompoType = parent.getCompType();
                    if("Window".equals(parentCompoType)){
                        Window parentW = (Window) parent;
                        parentW.removeClass(layer);
                    }
                    else if("Class".equals(parentCompoType)){
                        Class parentC = (Class) parent;
                        parentC.removeClass(layer);
                    }
                }
                else if("Function".equals(lsatLayerCompoType)){
                    Function layer = (Function) lastLayer;
                    Component parent = layer.parent;
                    String parentCompoType = parent.getCompType();
                    if("Class".equals(parentCompoType)){
                        Class parentC = (Class) parent;
                        parentC.removeMethod(layer);
                    }
                }

                else if("Variable".equals(lsatLayerCompoType)){
                    Variable layer = (Variable) lastLayer;
                    Component parent = layer.parent;
                    String parentCompoType = parent.getCompType();
                    if("Class".equals(parentCompoType)){
                        Class parentC = (Class) parent;
                        parentC.removeAttribute(layer);
                    }
                    else if("Function".equals(parentCompoType)){
                        Function parentC = (Function) parent;
                        parentC.removeParameter(layer);
                    }
                }
                window.popWindowLayers();
                setTabsBox();
            }
        }
    }

    public void textDialogWindow(String title, String ContentText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ContentText);
        alert.showAndWait();
    }

    public void helpDialogWindow(){
        textDialogWindow("Help", "For help call 911");
    }

    public void aboutDialogWindow(){
        textDialogWindow("About", "Check my github\nhttps://github.com/ar-ekt");
    }

    public void settingDialogWindow(){
        textDialogWindow("Setting", "Not supported for now");
    }

    public void fileMenuItems(Menu fileMenu){
        MenuItem newMenuItem = new MenuItem("New");
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem closeMenuItem = new MenuItem("Close");
        MenuItem closeAllMenuItem = new MenuItem("Close All");
        MenuItem saveMenuItem = new MenuItem("Save");
        MenuItem saveAsMenuItem = new MenuItem("Save As");
        MenuItem saveAllMenuItem = new MenuItem("Save All");
        MenuItem saveToJavaMenuItem = new MenuItem("Convert & Save in Java");
        MenuItem settingMenuItem = new MenuItem("Setting");
        MenuItem exitMenuItem = new MenuItem("Exit");

        newMenuItem.setOnAction(event -> newWindow());
        openMenuItem.setOnAction(event -> openWindow());
        closeMenuItem.setOnAction(event -> closeCurrentWindow());
        closeAllMenuItem.setOnAction(event -> closeAllWindows());
        saveMenuItem.setOnAction(event -> saveCurrentWindow());
        saveAsMenuItem.setOnAction(event -> saveAsCurrentWindow());
        saveAllMenuItem.setOnAction(event -> saveAllWindows());
        saveToJavaMenuItem.setOnAction(event -> saveToJava());
        settingMenuItem.setOnAction(event -> settingDialogWindow());
        exitMenuItem.setOnAction(event -> exit());

        fileMenu.getItems().addAll(newMenuItem, openMenuItem,
                new SeparatorMenuItem(),
                closeMenuItem, closeAllMenuItem, saveMenuItem, saveAsMenuItem, saveAllMenuItem,
                new SeparatorMenuItem(),
                saveToJavaMenuItem,
                new SeparatorMenuItem(),
                settingMenuItem, exitMenuItem);
    }

    public void editMenuItems(Menu editMenu){
        MenuItem previousComponentMenuItem = new MenuItem("Previous Component");
        MenuItem editComponentMenuItem = new MenuItem("Edit Component");
        MenuItem duplicateComponentMenuItem = new MenuItem("Duplicate Component");
        MenuItem deleteComponentMenuItem = new MenuItem("Delete Component");

        previousComponentMenuItem.setOnAction(event -> removeLastLayerCurrentWindow());
        editComponentMenuItem.setOnAction(event -> editLastLayerCurrentWindow());
        duplicateComponentMenuItem.setOnAction(event -> duplicateLastLayerCurrentWindow());
        deleteComponentMenuItem.setOnAction(event -> deleteLastLayerCurrentWindow());

        editMenu.getItems().addAll(previousComponentMenuItem,
                editComponentMenuItem,
                duplicateComponentMenuItem,
                deleteComponentMenuItem);
    }

    public void componentsMenuItems(Menu componentsMenu){
        MenuItem classMenuItem = new MenuItem("Class");
        MenuItem functionMenuItem = new MenuItem("Function");
        MenuItem variableMenuItem = new MenuItem("Variable");

        classMenuItem.setOnAction(event -> makeNewClass());
        functionMenuItem.setOnAction(event -> makeNewFunction());
        variableMenuItem.setOnAction(event -> makeNewVariable());

        componentsMenu.getItems().addAll(classMenuItem, functionMenuItem, variableMenuItem);
    }

    public void helpMenuItems(Menu helpMenu){
        MenuItem helpMenuItem = new MenuItem("Help");
        MenuItem aboutMenuItem = new MenuItem("About");

        helpMenuItem.setOnAction(event -> helpDialogWindow());
        aboutMenuItem.setOnAction(event -> aboutDialogWindow());

        helpMenu.getItems().addAll(helpMenuItem, aboutMenuItem);
    }

    public void setMenuBar(){
        Menu fileMenu = new Menu("File");
        fileMenuItems(fileMenu);

        Menu editMenu = new Menu("Edit");
        editMenuItems(editMenu);

        Menu componentsMenu = new Menu("Components");
        componentsMenuItems(componentsMenu);

        Menu helpMenu = new Menu("Help");
        helpMenuItems(helpMenu);

        Object temp = menuBar.getMenus().addAll(fileMenu, editMenu, componentsMenu, helpMenu);
    }

    public static void setTabsBox(){
        int index = (windows.size() > 0) ? tabsBox.getTabs().indexOf(Objects.requireNonNull(currentWindow()).tab) : 0;
        tabsBox.getTabs().clear();
        for (Window window: windows) {
            window.setTab();
            tabsBox.getTabs().add(window.tab);
        }
        if(windows.size() > 0)
            tabsBox.getSelectionModel().select(index);
    }
}