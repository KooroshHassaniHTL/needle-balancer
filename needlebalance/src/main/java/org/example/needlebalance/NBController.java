package org.example.needlebalance;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.Random;

public class NBController {
    /*
        @FXML private Button leftButton;
        @FXML private Button rightButton;
        @FXML private Rectangle backgroundBar;
         */
    @FXML private Button startButton;
    @FXML private Line balanceMarker;
    @FXML private Rectangle safeZone;
    @FXML public Label scoreLabel;
    @FXML public Pane balancePane;
    @FXML private AnchorPane scene;
    private int score = 0;
    private boolean leftHold = false;
    private boolean rightHold = false;
    private boolean randomizePosition = false;
    private double needleX = BAR_WIDTH/2; // start position in the middle of bar
    public static final double BAR_WIDTH = 500; // if you change this also change fxml accordingly
    private static final double NEEDLE_TICKSPEED = 0.2;
    private static final int RANDOMIZER_MODIFIER = 4;

    private AnimationTimer animationTimer;


    @FXML
    private void initialize() {
        /*
        startButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER){
                balanceModel.randomizePosition();
                System.out.println("STARTED");
                startButton.setVisible(false);
            }
        });
        */

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER){
                startButton.setVisible(false);
                randomizePosition = true;
            }
        });

        // make sure anchorpane can receive keyboard focus
        scene.setFocusTraversable(true);

        // request focus after scene is attached
        scene.sceneProperty().addListener((observer, oldScene, newScene) -> {
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
                    moveNeedle(-NEEDLE_TICKSPEED);
                }
                if (rightHold) {
                    moveNeedle(NEEDLE_TICKSPEED);
                }
                if (randomizePosition) {
                    int posRandom = (int) (Math.random() * RANDOMIZER_MODIFIER);
                    int negRandom = (int) -(Math.random() * RANDOMIZER_MODIFIER);
                    double randomizer = Math.random();
                    if (randomizer < 0.5) moveNeedle(posRandom);
                    else moveNeedle(negRandom);
                }
                if (isInSafeZone() && randomizePosition){
                    score += 1;
                    scoreLabel.setText("Score: " + score);
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

    private boolean isInSafeZone() {
       return needleX >= (BAR_WIDTH/2) - 40 && needleX <= (BAR_WIDTH/2) + 40;
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
