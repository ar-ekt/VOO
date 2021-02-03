import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.util.List;

public abstract class Component {
    public List<Component> allComponents;

    public double textWidth(String string, double fontSize){
        Text text = new Text(string);
        text.setFont(Font.font(fontSize));
        return text.getLayoutBounds().getWidth();
    }

    public abstract String getCompType();
    public abstract String getName();
    public abstract String getNameView();

    public abstract JSONObject toJson();
    public abstract String toJava(String indentation);

    public abstract void showAsWindow(AnchorPane group);
    public abstract void detailsShow(AnchorPane group);
}