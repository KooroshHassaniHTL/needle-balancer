package org.example.needlebalance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NBApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NBApplication.class.getResource("needlebalance-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 350);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
