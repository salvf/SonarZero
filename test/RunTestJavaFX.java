import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import java.io.File;
import sonarzero.api.ui.waveform.jfx.WaveformGenerator;


public class RunTestJavaFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Waveform-Ganerator-JavaFX-Test");
        Group root = new Group();
        Canvas canvas = new Canvas(700, 450);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("\\Waltz.wav");
        WaveformGenerator wf = new WaveformGenerator(new File(filePath), gc);
        wf.draw();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
