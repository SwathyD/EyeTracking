package eyetracker;

import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

class Controller {

    MainWindow mainWindow;
    CameraAdapter cameraIp;

    Controller() {
        mainWindow = new MainWindow();
    }

    public void useMobileCam() {
        cameraIp = new NetworkInput();
    }

    public void useWebCam() {
        cameraIp = new WebCamHandler();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXHelloCV.fxml"));
        Test1 controller = loader.getController();
    }

    public void start(Stage primaryStage) {
        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXHelloCV.fxml"));
            // store the root element so that the controllers can use it
            BorderPane rootElement = (BorderPane) loader.load();
            // create and style a scene
            Scene scene = new Scene(rootElement, 800, 600);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("JavaFX meets OpenCV");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // set the proper behavior on closing the application
            Test1 controller = loader.getController();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    controller.setClosed();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        
    }
    
    void startTracking(){
        
    }
}
