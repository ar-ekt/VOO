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

public class Function extends Component {
    public final String componentType = "Function";

    public String name;
    public String returnType;
    public String privilege;
    public List<String> statuses = new ArrayList<>();
    int parameterN;
    public List<Variable> parameters = new ArrayList<>();
    public String body;
    public Component parent;

    public Function(String name, String returnType, Component parent){
        this.name = name;
        this.returnType = returnType;
        this.body = "";
        this.parent = parent;
        allComponents = new ArrayList<>();
        this.parameterN = 0;
    }
    public Function(Function func){
        this.name = func.name;
        this.returnType = func.returnType;
        this.privilege = func.privilege;
        this.parameterN = func.parameterN;
        this.body = func.body;
        this.parent = func.parent;

        this.parameters = new ArrayList<>(func.parameters);
        this.statuses = new ArrayList<>(func.statuses);
        this.allComponents = new ArrayList<>(func.allComponents);
    }
    public Function(JSONObject json, Component parent){
        this(json.getString("name"), json.getString("return-type"), parent);
        this.privilege = json.getString("privilege");
        this.body = json.getString("body");
        for (Object subJson: json.getJSONArray("statuses")){ this.addStatus((String) subJson); }
        for (Object subJson: json.getJSONArray("parameters")){ this.addParameter(new Variable((JSONObject) subJson, this)); }
    }

    public String getCompType(){ return this.componentType; }
    public String getName(){ return this.name; }
    public String getNameView(){
        String string = this.returnType + " " + this.name + "(";
        if(parameters.size() != 0) {
            string += String.format("%s %s", parameters.get(0).getType(), parameters.get(0).getName());
            for (Variable parameter : parameters.subList(1, parameters.size())) {
                string += String.format(", %s %s", parameter.getType(), parameter.getName());
            }
        }
        string += ")";
        return string;
    }

    public void addStatus(String status){ this.statuses.add(status); }
    public void removeStatus(String status){ this.statuses.remove(status); }

    public void addParameter(Variable parameter){
        parameterN++;
        this.parameters.add(parameter);
        this.allComponents.add(parameter);
    }
    public void removeParameter(Variable parameter){
        parameterN--;
        this.parameters.remove(parameter);
        this.allComponents.remove(parameter);
    }

    public String toJava(String indentation) {
        String text = "";
        text += indentation;
        text += this.privilege;
        for(String status: this.statuses) text += " " + status;
        text += " " + this.returnType;
        text += " " + this.name + "(";

        if(this.parameters.size() > 0) {
            text += this.parameters.get(0).toJava(indentation + "\t");
            for(Variable parameter: this.parameters) { text += ", " + parameter.toJava(indentation + "\t"); };
        }

        text += "){";
        for (String line: this.body.split("\n")){ text += "\n\t" + indentation + line; }
        text += "\n" + indentation + "}";

        return text;
    }

    public JSONObject toJson() {
        JSONObject items = new JSONObject();

        items.put("name", this.getName());
        items.put("component-type", this.getCompType());
        items.put("return-type", this.returnType);
        items.put("privilege", this.privilege);

        JSONArray statusesJsonArray = new JSONArray();
        for(String status: this.statuses){ statusesJsonArray.put(status); }
        items.put("statuses", statusesJsonArray);

        JSONArray parametersJsonArray = new JSONArray();
        for(Variable parameter: this.parameters){ parametersJsonArray.put(parameter.toJson()); }
        items.put("parameters", parametersJsonArray);

        items.put("body", this.body);

        return items;
    }

    public void detailsShow(AnchorPane group){
        double fontSize = 15;

        Label componentLabel = new Label(this.getCompType());
        componentLabel.setFont(Font.font(fontSize));

        Label nameLabel = new Label("Name: " + this.name);
        nameLabel.setFont(Font.font(fontSize));

        Label returnTypeLabel = new Label("Return Type: " + this.returnType);
        returnTypeLabel.setFont(Font.font(fontSize));

        Label privilegeLabel = new Label("Privilege: " + this.privilege);
        privilegeLabel.setFont(Font.font(fontSize));

        String statusesString = "Statuses:";
        for(String status: this.statuses){ statusesString += "\n    " + status; }
        Label statusesLabel = new Label(statusesString);
        statusesLabel.setFont(Font.font(fontSize));

        String parametersString = "Parameters:";
        for(Variable parameter: this.parameters){ parametersString += "\n    " + parameter.getType() + " " + parameter.getName(); }
        Label parametersLabel = new Label(parametersString);
        parametersLabel.setFont(Font.font(fontSize));

        VBox box = new VBox(5);
        box.getChildren().addAll(componentLabel, nameLabel, returnTypeLabel, privilegeLabel, statusesLabel, parametersLabel);
        group.getChildren().addAll(box);
    }

    public double showParameters(AnchorPane group, double fontSize) {
        HBox parasShow = new HBox();
        double width = 0;
        if(parameters.size() != 0) {
            AnchorPane groupP = new AnchorPane();
            width += parameters.get(0).showAsParameter(groupP, false, fontSize);
            parasShow.getChildren().add(groupP);
            for (Variable parameter : parameters.subList(1, parameters.size())) {
                groupP = new AnchorPane();
                width += parameter.showAsParameter(groupP, true, fontSize);
                parasShow.getChildren().add(groupP);
            }
        }

        group.getChildren().addAll(parasShow);
        return width;
    }

    public double showAsMethod(AnchorPane group){
        return show(group, 18);
    }

    public double showAsBox(AnchorPane group){
        double fontSize = 30;
        double height = 45;
        double width = textWidth(this.returnType+" ", fontSize) + textWidth(this.name, fontSize) + 20;
        for(Variable parameter: parameters){
            width += textWidth(parameter.type+" ", fontSize);
            width += textWidth(parameter.name, fontSize);
        }
        width += (Math.max(parameters.size() - 1, 0)) * textWidth(", ", fontSize) + textWidth("()", fontSize);

        AnchorPane texts = new AnchorPane();
        show(texts, fontSize);

        Rectangle border = new Rectangle(0.0d, 0.0d, width+4, height+4);
        border.setFill(Color.BLACK);
        AnchorPane.setLeftAnchor(border, -2.0);
        AnchorPane.setTopAnchor(border, -2.0);

        Rectangle background = new Rectangle(0.0d, 0.0d, width, height);
        background.setFill(Color.rgb(226 ,160,0));

        AnchorPane.setLeftAnchor(texts, 10.0);

        group.getChildren().addAll(border, background, texts);

        return width;
    }

    public void showAsWindow(AnchorPane group){
        double fontSize = 20;

        AnchorPane groupF = new AnchorPane();
        showAsBox(groupF);
        AnchorPane.setLeftAnchor(groupF, 40.0);
        AnchorPane.setTopAnchor(groupF, 40.0);

        Label bodyL = new Label("Body:");
        bodyL.setFont(Font.font(30));
        AnchorPane.setLeftAnchor(bodyL, 40.0);
        AnchorPane.setTopAnchor(bodyL, 200.0-50);

        VBox groupB = new VBox();
        String[] bodyLines = this.body.split("\n");
        double max_width=0, bodyLinesN=0;
        for(String line: bodyLines){
            Label lineL = new Label(line);
            lineL.setFont(Font.font(fontSize));
            groupB.getChildren().add(lineL);
            max_width = Math.max(max_width, textWidth(line, fontSize));
            bodyLinesN++;
        }
        bodyLinesN = Math.max(bodyLinesN, 1);
        AnchorPane.setLeftAnchor(groupB, 40.0);
        AnchorPane.setTopAnchor(groupB, 200.0);

        Rectangle bodyBorder = new Rectangle(0.0d, 0.0d, max_width+20+4, bodyLinesN*32+4);
        bodyBorder.setFill(Color.BLACK);
        AnchorPane.setLeftAnchor(bodyBorder, 40.0-10-2);
        AnchorPane.setTopAnchor(bodyBorder, 200.0-5-2);

        Rectangle bodyRect = new Rectangle(0.0d, 0.0d, max_width+20, bodyLinesN*32);
        bodyRect.setFill(Color.rgb(200 ,200,200));
        AnchorPane.setLeftAnchor(bodyRect, 40.0-10);
        AnchorPane.setTopAnchor(bodyRect, 200.0-5);

        group.getChildren().addAll(groupF, bodyL, bodyBorder, bodyRect, groupB);
    }

    public double show(AnchorPane group, double fontSize){
        double width = textWidth(this.returnType+" ", fontSize) + textWidth(this.name+"()", fontSize);
        Label typeLabel = new Label(this.returnType+" ");
        typeLabel.setFont(Font.font(fontSize));
        typeLabel.setTextFill(Color.rgb(0,80,80));

        Label nameLabel = new Label(String.format("%s(", this.name));
        nameLabel.setFont(Font.font(fontSize));
        nameLabel.setTextFill(Color.BLACK);

        AnchorPane parasLabel = new AnchorPane();
        width += showParameters(parasLabel, fontSize);

        Label parenthesesCloseLabel = new Label(")");
        parenthesesCloseLabel.setFont(Font.font(fontSize));
        parenthesesCloseLabel.setTextFill(Color.BLACK);

        HBox box = new HBox();
        box.getChildren().addAll(typeLabel, nameLabel, parasLabel, parenthesesCloseLabel);

        group.getChildren().addAll(box);
        return width;
    }
}
