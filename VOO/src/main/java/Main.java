import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage stage){
        Screen screen = new Screen(stage);
        screen.run();
    }

    public static void main(String[] args) {
        launch(args);
    }
}