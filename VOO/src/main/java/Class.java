import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Class extends Component {
    public final String componentType = "Class";

    public String name;
    public String type; // class, interface, enum
    public String privilege;
    public List<String> statuses = new ArrayList<>();

    public List<String> parents = new ArrayList<>();

    int classN, attributeN, methodN;
    public List<Class> classes = new ArrayList<>();
    public List<Function> methods = new ArrayList<>();
    public List<Variable> attributes = new ArrayList<>();
    public Component parent;

    public Class(String name, String type, Component parent){
        this.name = name;
        this.type = type;
        this.parent = parent;
        allComponents = new ArrayList<>();
        this.classN = 0;
        this.attributeN = 0;
        this.methodN = 0;
    }
    public Class(Class class_){
        this.name = class_.name;
        this.type = class_.type;
        this.parent = class_.parent;
        this.privilege = class_.privilege;
        this.classN = class_.classN;
        this.attributeN = class_.attributeN;
        this.methodN = class_.methodN;
        this.allComponents = new ArrayList<>(class_.allComponents);
        this.classes = new ArrayList<>(class_.classes);
        this.methods = new ArrayList<>(class_.methods);
        this.attributes = new ArrayList<>(class_.attributes);
        this.statuses = new ArrayList<>(class_.statuses);
        this.parents = new ArrayList<>(class_.parents);
    }
    public Class(JSONObject json, Component parent){
        this(json.getString("name"), json.getString("type"), parent);
        this.privilege = json.getString("privilege");
        for (Object subJson: json.getJSONArray("statuses")){ this.addStatus((String) subJson); }
        for (Object subJson: json.getJSONArray("parents")){ this.addParent(subJson.toString()); }
        for (Object subJson: json.getJSONArray("inner-classes")){ this.addClass(new Class((JSONObject) subJson, this)); }
        for (Object subJson: json.getJSONArray("methods")){ this.addMethod(new Function((JSONObject) subJson, this)); }
        for (Object subJson: json.getJSONArray("attributes")){ this.addAttribute(new Variable((JSONObject) subJson, this)); }
    }

    public String getCompType(){ return this.componentType; }
    public String getName() { return this.name; }
    public String getNameView(){ return this.name; }

    public void addStatus(String status){ this.statuses.add(status); }
    public void addParent(String parent){ this.parents.add(parent); }
    public void addClass(Class class_){
        classN++;
        this.classes.add(class_);
        this.allComponents.add(class_);
    }
    public void removeClass(Class class_){
        classN--;
        this.classes.remove(class_);
        this.allComponents.remove(class_);
    }

    public void addMethod(Function method){
        methodN++;
        this.methods.add(method);
        this.allComponents.add(method);
    }
    public void removeMethod(Function method){
        methodN--;
        this.methods.remove(method);
        this.allComponents.remove(method);
    }

    public void addAttribute(Variable attribute){
        attributeN++;
        this.attributes.add(attribute);
        this.allComponents.add(attribute);
    }
    public void removeAttribute(Variable attribute){
        attributeN--;
        this.attributes.remove(attribute);
        this.allComponents.remove(attribute);
    }

    public String toJava(String indentation) {
        String text = "";
        text += indentation;
        text += this.privilege;
        for(String status: this.statuses){ text += " " + status; }
        text += " " + this.type;
        text += " " + this.name;
        if(this.parents.size() != 0){
            text += " extends " + this.parents.get(0);
            if(this.parents.size() > 1)
                for (String parent_: this.parents.subList(1, this.parents.size())) { text += ", " + parent_; }
        }
        text += "{";
        for(Variable attribute: this.attributes){ text += "\n" + attribute.toJava(indentation + "\t"); }
        for(Class class_: this.classes){ text += "\n\n" + class_.toJava(indentation + "\t"); }
        for(Function method: this.methods) text += "\n\n" + method.toJava(indentation + "\t");
        text += "\n" + indentation + "}";
        return text;
    }

    public JSONObject toJson(){
        JSONObject items = new JSONObject();

        items.put("name", this.getName());
        items.put("component-type", this.getCompType());
        items.put("type", this.type);
        items.put("privilege", this.privilege);

        JSONArray statusesJsonArray = new JSONArray();
        for(String status: this.statuses){ statusesJsonArray.put(status); }
        items.put("statuses", statusesJsonArray);

        JSONArray parentsJsonArray = new JSONArray();
        for(String parent: this.parents){ parentsJsonArray.put(parent); }
        items.put("parents", parentsJsonArray);

        JSONArray attributesJsonArray = new JSONArray();
        for(Variable attribute: this.attributes){ attributesJsonArray.put(attribute.toJson()); }
        items.put("attributes", attributesJsonArray);

        JSONArray methodsJsonArray = new JSONArray();
        for(Function method: this.methods){ methodsJsonArray.put(method.toJson());}
        items.put("methods", methodsJsonArray);

        JSONArray classesJsonArray = new JSONArray();
        for(Class inner_class: this.classes){ classesJsonArray.put(inner_class.toJson()); }
        items.put("inner-classes", classesJsonArray);

        return items;
    }

    public double showAsInnerClass(AnchorPane group){
        double width = 0;
        Label nameLabel = new Label(this.name);
        nameLabel.setFont(Font.font(18));
        nameLabel.setTextFill(Color.BLACK);

        HBox box = new HBox();
        box.getChildren().addAll(nameLabel);

        group.getChildren().addAll(box);
        return width;
    }

    public double innerClassesShow(AnchorPane group){
        double max_width = 0, x;
        if(classes.size() != 0){
            VBox classesShow = new VBox();
            for (Class cls : classes) {
                AnchorPane groupC = new AnchorPane();
                x = cls.showAsInnerClass(groupC);
                classesShow.getChildren().add(groupC);
                max_width = Math.max(max_width, x);
            }
            classesShow.setSpacing(5);
            group.getChildren().addAll(classesShow);
        }else{
            Label noCLassesLabel = new Label("No Class");
            noCLassesLabel.setTextFill(Color.rgb(90, 90, 90));
            noCLassesLabel.setFont(Font.font(18));
            AnchorPane.setLeftAnchor(noCLassesLabel, 0.0);
            AnchorPane.setRightAnchor(noCLassesLabel, 0.0);
            noCLassesLabel.setAlignment(Pos.CENTER);
            group.getChildren().addAll(noCLassesLabel);
        }

        return max_width;
    }

    public double methodsShow(AnchorPane group){
        double max_width = 0, x;

        if(methods.size() != 0){
            VBox methodsShow = new VBox();
            for (Function method : methods) {
                AnchorPane groupM = new AnchorPane();
                x = method.showAsMethod(groupM);
                methodsShow.getChildren().add(groupM);
                max_width = Math.max(max_width, x);
            }
            methodsShow.setSpacing(5);
            group.getChildren().addAll(methodsShow);
        }else{
            Label noMethodsLabel = new Label("No Method");
            noMethodsLabel.setTextFill(Color.rgb(90, 90, 90));
            noMethodsLabel.setFont(Font.font(18));
            AnchorPane.setLeftAnchor(noMethodsLabel, 0.0);
            AnchorPane.setRightAnchor(noMethodsLabel, 0.0);
            noMethodsLabel.setAlignment(Pos.CENTER);
            group.getChildren().addAll(noMethodsLabel);
        }

        return max_width;
    }

    public double attributesShow(AnchorPane group){
        double max_width = 0, x;
        if(attributes.size() != 0){
            VBox attrsShow = new VBox();
            for (Variable attr : attributes) {
                AnchorPane groupA = new AnchorPane();
                x = attr.showAsAttribute(groupA);
                attrsShow.getChildren().add(groupA);
                max_width = Math.max(max_width, x);
            }
            attrsShow.setSpacing(5);
            group.getChildren().addAll(attrsShow);
        }else{
            Label noAttrsLabel = new Label("No Attribute");
            noAttrsLabel.setTextFill(Color.rgb(90, 90, 90));
            noAttrsLabel.setFont(Font.font(18));
            AnchorPane.setLeftAnchor(noAttrsLabel, 0.0);
            AnchorPane.setRightAnchor(noAttrsLabel, 0.0);
            noAttrsLabel.setAlignment(Pos.CENTER);
            group.getChildren().addAll(noAttrsLabel);
        }

        return max_width;
    }

    public double showAsBox(AnchorPane group){
        double max_width = 300, x;
        double height;
        double extraY = 0;
        double nLineY, cLineY, mLineY;

        Label nameLabel = new Label(this.getName());
        nameLabel.setFont(Font.font(30));
        AnchorPane.setLeftAnchor(nameLabel, 0.0);
        AnchorPane.setRightAnchor(nameLabel, 0.0);
        nameLabel.setAlignment(Pos.CENTER);
        x = textWidth(this.getName(), 30);
        max_width = Math.max(max_width, x);
        extraY += 45;

        nLineY = extraY;
        extraY += 5;

        AnchorPane innerClassesShow = new AnchorPane();
        x = innerClassesShow(innerClassesShow);
        AnchorPane.setTopAnchor(innerClassesShow, extraY);
        AnchorPane.setLeftAnchor(innerClassesShow, 5.0);
        max_width = Math.max(max_width, x);
        extraY += (Math.max(classes.size(), 1) * 35) - 5;

        cLineY = extraY;
        extraY += 5;

        AnchorPane methodsShow = new AnchorPane();
        x = methodsShow(methodsShow);
        AnchorPane.setTopAnchor(methodsShow, extraY);
        AnchorPane.setLeftAnchor(methodsShow, 5.0);
        max_width = Math.max(max_width, x);
        extraY += (Math.max(methods.size(), 1) * 35) - 5;

        mLineY = extraY;
        extraY += 5;

        AnchorPane attrsShow = new AnchorPane();
        x = attributesShow(attrsShow);
        AnchorPane.setTopAnchor(attrsShow, extraY);
        AnchorPane.setLeftAnchor(attrsShow, 5.0);
        max_width = Math.max(max_width, x);
        extraY += (Math.max(attributes.size(), 1) * 35) - 5;

        height = extraY;

        Line nameLine = new Line(0.0, nLineY, max_width, nLineY);
        nameLine.setFill(Color.BLACK);
        nameLine.setStrokeWidth(2);

        Line innerClassesLine = new Line(0.0, cLineY, max_width, cLineY);
        innerClassesLine.setFill(Color.BLACK);
        innerClassesLine.setStrokeWidth(2);

        Line methodsLine = new Line(0.0, mLineY, max_width, mLineY);
        methodsLine.setFill(Color.BLACK);
        methodsLine.setStrokeWidth(2);

        Rectangle border = new Rectangle(0.0d, 0.0d, max_width+4, height+4);
        border.setFill(Color.BLACK);
        AnchorPane.setLeftAnchor(border, -2.0);
        AnchorPane.setTopAnchor(border, -2.0);

        Rectangle background = new Rectangle(0.0d, 0.0d, max_width, height);
        background.setFill(Color.rgb(0 ,177,37));
        AnchorPane.setLeftAnchor(background, 0.0);
        AnchorPane.setTopAnchor(background, 0.0);

        group.getChildren().addAll(border, background,
                nameLabel, nameLine,
                innerClassesShow, innerClassesLine,
                methodsShow, methodsLine,
                attrsShow);

        return max_width;
    }

    public void detailsShow(AnchorPane group){
        double fontSize = 15;

        Label componentLabel = new Label(this.getCompType());
        componentLabel.setFont(Font.font(fontSize));

        Label nameLabel = new Label("Name: " + this.name);
        nameLabel.setFont(Font.font(fontSize));

        Label typeLabel = new Label("Return Type: " + this.type);
        typeLabel.setFont(Font.font(fontSize));

        Label privilegeLabel = new Label("Privilege: " + this.privilege);
        privilegeLabel.setFont(Font.font(fontSize));

        String statusesString = "Statuses:";
        for(String status: this.statuses){ statusesString += "\n    " + status; }
        Label statusesLabel = new Label(statusesString);
        statusesLabel.setFont(Font.font(fontSize));

        String parentsString = "Parents:";
        if(this.parents.size() == 0){
            parentsString += "\n    " + "-------";
        }else {
            for(String parent: this.parents){ parentsString += "\n    " + parent; }
        }
        Label parentsLabel = new Label(parentsString);
        parentsLabel.setFont(Font.font(fontSize));

        String innerClassesString = "Inner Classes:";
        if(this.classes.size() == 0){
            innerClassesString += "\n    " + "-------";
        }else {
            for (Class cls : this.classes) { innerClassesString += "\n    " + cls.getName(); }
        }
        Label innerClassesLabel = new Label(innerClassesString);
        innerClassesLabel.setFont(Font.font(fontSize));

        String methodsString = "Methods:";
        if(this.methods.size() == 0){
            methodsString += "\n    " + "-------";
        }else {
            for (Function method : this.methods) {
                methodsString += "\n    " + method.returnType + " " + method.getName() + "(";
                for (Variable parameter : method.parameters) {
                    methodsString += parameter.getType() + " " + parameter.getName() + ", ";
                }
                methodsString += ")";
            }
        }
        Label methodsLabel = new Label(methodsString);
        methodsLabel.setFont(Font.font(fontSize));

        String attributesString = "Attributes:";
        if(this.attributes.size() == 0){
            attributesString += "\n    " + "-------";
        }else {
            for (Variable attr : this.attributes) { attributesString += "\n    " + attr.getType() + " " + attr.getName(); }
        }
        Label attributesLabel = new Label(attributesString);
        attributesLabel.setFont(Font.font(fontSize));

        VBox box = new VBox(5);
        box.getChildren().addAll(componentLabel, nameLabel, typeLabel, privilegeLabel, statusesLabel, parentsLabel, innerClassesLabel, methodsLabel, attributesLabel);
        group.getChildren().addAll(box);
    }

    public void showAsWindow(AnchorPane group){
        double extra_x = 40.0, extra_x1;
        double max_width = 0;
        for(Variable attr: attributes) {
            AnchorPane groupA = new AnchorPane();
            extra_x1 = attr.showAsBox(groupA);
            AnchorPane.setLeftAnchor(groupA, extra_x);
            AnchorPane.setTopAnchor(groupA, 50.0);
            group.getChildren().add(groupA);
            max_width = Math.max(max_width, extra_x1);
            extra_x += extra_x1 + 20;
        }

        extra_x = 40.0;
        for(Function method: methods) {
            AnchorPane groupM = new AnchorPane();
            extra_x1 = method.showAsBox(groupM);
            AnchorPane.setLeftAnchor(groupM, extra_x);
            AnchorPane.setTopAnchor(groupM, 200.0);
            group.getChildren().add(groupM);
            max_width = Math.max(max_width, extra_x1);
            extra_x += extra_x1 + 20;
        }

        extra_x = 40.0;
        for(Class class_: classes) {
            AnchorPane groupC = new AnchorPane();
            extra_x1 = class_.showAsBox(groupC);
            AnchorPane.setLeftAnchor(groupC, extra_x);
            AnchorPane.setTopAnchor(groupC, 350.0);
            group.getChildren().add(groupC);
            max_width = Math.max(max_width, extra_x1);
            extra_x += extra_x1 + 20;
        }

    }
}