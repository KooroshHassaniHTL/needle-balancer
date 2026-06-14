package org.example.needlebalance;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.example.needlebalance.Model.BalanceModel;

public class NBController {
    /*
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Rectangle safeZone;
    @FXML private Rectangle backgroundBar;
     */
    @FXML private Button startButton;
    @FXML private Line balanceMarker;
    @FXML public Pane balancePane;
    @FXML private AnchorPane scene;
    private boolean leftHold = false;
    private boolean rightHold = false;

    private double needleX = BAR_WIDTH/2; // start position in the middle of bar
    public static final double BAR_WIDTH = 500; // if you change this also change fxml accordingly
    private static final double ANIMATION_TICK = 1;

    private AnimationTimer animationTimer;
    private BalanceModel balanceModel;


    @FXML
    private void initialize() {
        balanceModel = new BalanceModel();

        startButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER){
                balanceModel.randomizePosition();
                System.out.println("STARTED");
                startButton.setVisible(false);
            }
        });

        // make sure anchorpane can receive keyboard focus
        scene.setFocusTraversable(true);

        // request focus after scene is attached
        scene.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                scene.requestFocus();
            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT){
                leftHold = true;
            }
            if ( event.getCode() == KeyCode.RIGHT){
                rightHold = true;
            }
        });

        scene.setOnKeyReleased(event -> {
           if (event.getCode() == KeyCode.LEFT) {
               leftHold = false;
           }
           if (event.getCode() == KeyCode.RIGHT) {
               rightHold = false;
           }
        });

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now){
                if (leftHold) {
                    moveNeedle(-ANIMATION_TICK);
                }
                if (rightHold) {
                    moveNeedle(ANIMATION_TICK);
                }
            }
        };

        animationTimer.start();


        /*
        leftButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.A) {
                moveNeedle(-5);
            }
        });

        rightButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.D) {
                moveNeedle(5);
            }
        });

         */
    }

    private void moveNeedle(double amount) {
        needleX += amount;  // set at planned coordinate
        clampNeedle();      // if that coord is out of bounds, clamp it so it's a valid position
        updateNeedle();     // set needle to that valid position
    }

    private void updateNeedle() {
        balanceMarker.setStartX(needleX);
        balanceMarker.setEndX(needleX);
    }

    private void clampNeedle() {
        double min = 0;
        double max = BAR_WIDTH;

        if (needleX < min) needleX = min;
        if (needleX > max) needleX = max;

    }

}
