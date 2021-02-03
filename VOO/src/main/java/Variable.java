import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Variable extends Component {
    public final String componentType = "Variable";

    public String name;
    public String type;
    public String privilege;
    public List<String> statuses = new ArrayList<>();
    public Component parent;

    public Variable(String name, String type, Component parent){
        this.name = name;
        this.type = type;
        this.parent = parent;
        allComponents = new ArrayList<>();
    }
    public Variable(Variable var){
        this.name = var.name;
        this.type = var.type;
        this.privilege = var.privilege;
        this.parent = var.parent;
        this.statuses = new ArrayList<>(var.statuses);
        this.allComponents = new ArrayList<>(var.allComponents);
    }
    public Variable(JSONObject json, Component parent){
        this(json.getString("name"), json.getString("type"), parent);
        this.privilege = json.getString("privilege");
        for (Object subJson: json.getJSONArray("statuses")){ this.addStatus((String) subJson); }
    }

    public String getCompType(){ return this.componentType; }
    public String getNameView(){ return this.type + " " + this.name; }
    public String getName(){ return this.name; }
    public String getType() { return this.type; }

    public void addStatus(String status){ this.statuses.add(status); }
    public void removeStatus(String status){ this.statuses.remove(status); }

    public String toJava(String indentation) {
        String text = "";
        for(String status: this.statuses){ text += " " + status; }
        text += " " + this.type;
        text += " " + this.name;
        if(this.parent.getCompType().equals("Class")) { text = indentation + this.privilege + text + ";"; }
        return text.toString();
    }

    public JSONObject toJson() {
        JSONObject items = new JSONObject();

        items.put("name", this.getName());
        items.put("component-type", this.getCompType());
        items.put("type", this.type);
        items.put("privilege", this.privilege);

        JSONArray statusesJsonArray = new JSONArray();
        for(String status: this.statuses){ statusesJsonArray.put(status); }
        items.put("statuses", statusesJsonArray);

        return items;
    }

    public void detailsShow(AnchorPane group){
        double fontSize = 15;

        Label componentLabel = new Label(this.getCompType());
        componentLabel.setFont(Font.font(fontSize));

        Label nameLabel = new Label("Name: " + this.name);
        nameLabel.setFont(Font.font(fontSize));

        Label typeLabel = new Label("Type: " + this.type);
        typeLabel.setFont(Font.font(fontSize));

        Label privilegeLabel = new Label("Privilege: " + this.privilege);
        privilegeLabel.setFont(Font.font(fontSize));

        String statusesString = "Statuses:";
        for(String status: this.statuses){ statusesString += "\n    " + status; }
        Label statusesLabel = new Label(statusesString);
        statusesLabel.setFont(Font.font(fontSize));

        VBox box = new VBox(5);
        box.getChildren().addAll(componentLabel, nameLabel, typeLabel, privilegeLabel, statusesLabel);
        group.getChildren().addAll(box);
    }

    public double showAsParameter(AnchorPane group, Boolean comma, double fontSize){
        return show(group, comma, fontSize);
    }

    public double showAsAttribute(AnchorPane group){
        return show(group, false, 18);
    }

    public double showAsBox(AnchorPane group){
        double fontSize = 30;
        double height = 45;

        AnchorPane texts = new AnchorPane();
        double width = show(texts, false, fontSize);

        Rectangle border = new Rectangle(0.0d, 0.0d, width+4, height+4);
        border.setFill(Color.BLACK);
        AnchorPane.setLeftAnchor(border, -2.0);
        AnchorPane.setTopAnchor(border, -2.0);

        Rectangle background = new Rectangle(0.0d, 0.0d, width, height);
        background.setFill(Color.rgb(208 ,0,186));

        AnchorPane.setLeftAnchor(texts, 10.0);

        group.getChildren().addAll(border, background, texts);

        return width;
    }

    public void showAsWindow(AnchorPane group){
        AnchorPane groupF = new AnchorPane();
        showAsBox(groupF);
        AnchorPane.setLeftAnchor(groupF, 40.0);
        AnchorPane.setTopAnchor(groupF, 40.0);

        group.getChildren().addAll(groupF);
    }

    public double show(AnchorPane group, Boolean comma, double fontSize){
        double width = textWidth(this.type+" ", fontSize) + textWidth(this.name, fontSize) + 20;

        String beforeStr = (comma) ? ", " : "";
        Label typeLabel = new Label(beforeStr+this.type+" ");
        typeLabel.setFont(Font.font(fontSize));
        typeLabel.setTextFill(Color.rgb(0,80,80));

        Label nameLabel = new Label(this.name);
        nameLabel.setFont(Font.font(fontSize));
        nameLabel.setTextFill(Color.BLACK);

        HBox box = new HBox();
        box.getChildren().addAll(typeLabel, nameLabel);
        group.getChildren().addAll(box);

        return width;
    }
}
