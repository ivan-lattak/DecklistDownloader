import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader((getClass().getResource("downloader.fxml")));
        Parent root = loader.load();
        Controller controller = loader.getController();

        primaryStage.setTitle("Decklist Downloader");
        primaryStage.setScene(new Scene(root, 500, 300));
        controller.setMainStage(primaryStage);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
