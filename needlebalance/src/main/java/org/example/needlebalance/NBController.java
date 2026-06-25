package org.example.needlebalance;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class NBController {

    @FXML private AnchorPane scene;
    @FXML private Button startButton;

    @FXML private Pane balancePane;
    @FXML private Line balanceMarker;
    @FXML private Rectangle safeZone;

    @FXML private Group playerGroup;
    @FXML private Group rockGroup;

    @FXML private Label scoreLabel;
    @FXML private Label statusLabel;
    @FXML private Label rockLabel;
    @FXML private Label fallWarningLabel;

    @FXML private ProgressBar scoreProgressBar;

    public static final double BAR_WIDTH = 500;
    private static final double CENTER_X = BAR_WIDTH / 2.0;

    private static final double START_SAFE_ZONE_WIDTH = 95;
    private static final double MIN_SAFE_ZONE_WIDTH = 42;

    private static final double MAX_NEEDLE_OFFSET = BAR_WIDTH / 2.0;
    private static final double FALL_LIMIT_OFFSET = 225;

    private static final double INPUT_FORCE = 0.035;
    private static final double NEEDLE_FRICTION = 0.984;

    private static final double BASE_DRIFT_STRENGTH = 0.012;
    private static final double BASE_WOBBLE_STRENGTH = 0.03;

    private static final double STABILITY_GAIN = 0.0005;
    private static final double STABILITY_LOSS = 0.0014;

    private static final double FALL_TIMER_GAIN = 0.018;
    private static final double FALL_TIMER_RECOVERY = 0.010;

    private final DoubleProperty stabilityProperty = new SimpleDoubleProperty(0);

    private AnimationTimer animationTimer;

    private boolean leftHold = false;
    private boolean rightHold = false;
    private boolean running = false;
    private boolean jumping = false;
    private boolean fallen = false;

    private int rockNumber = 1;

    private double needleX = CENTER_X;
    private double needleVelocity = 0;
    private double driftDirection = 1;
    private double windTimer = 0;
    private double fallTimer = 0;

    @FXML
    private void initialize() {
        scene.setFocusTraversable(true);

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

            if (event.getCode() == KeyCode.ENTER && !running) {
                startButton.fire();
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

        startButton.addEventHandler(ActionEvent.ACTION, event -> startGame());

        scoreProgressBar.progressProperty().bind(stabilityProperty);

        updateSafeZone();
        updateVisuals();

        animationTimer = new AnimationTimer() {
            private long previousTime = 0;

            @Override
            public void handle(long now) {
                if (previousTime == 0) {
                    previousTime = now;
                    return;
                }

                double deltaSeconds = (now - previousTime) / 1_000_000_000.0;
                previousTime = now;

                updateGame(deltaSeconds);
            }
        };

        animationTimer.start();
    }

    private void updateGame(double deltaSeconds) {
        if (!running || jumping || fallen) {
            return;
        }

        updateBalancePhysics(deltaSeconds);
        updateStability();
        updateFallPressure();
        updateVisuals();

        if (fallTimer >= 1.0) {
            fallOffRock();
            return;
        }

        if (stabilityProperty.get() >= 1.0) {
            jumpToNextRock();
        }
    }

    private void updateBalancePhysics(double deltaSeconds) {
        double difficulty = getDifficulty();

        windTimer -= deltaSeconds;
        if (windTimer <= 0) {
            driftDirection = Math.random() < 0.5 ? -1 : 1;
            windTimer = 0.65 + Math.random() * 1.25;
        }

        double distanceFromCenter = (needleX - CENTER_X) / MAX_NEEDLE_OFFSET;

        /*
         * The farther you lean, the more the game wants to keep tipping you.
         * This makes it feel like momentum instead of a simple slider.
         */
        double tippingForce = distanceFromCenter * BASE_WOBBLE_STRENGTH * difficulty;
        double windForce = driftDirection * BASE_DRIFT_STRENGTH * difficulty;

        needleVelocity += tippingForce + windForce;

        if (leftHold) {
            needleVelocity -= INPUT_FORCE;
        }

        if (rightHold) {
            needleVelocity += INPUT_FORCE;
        }

        needleVelocity *= NEEDLE_FRICTION;

        needleX += needleVelocity * 60 * deltaSeconds;
        clampNeedle();
    }

    private void updateStability() {
        if (isInSafeZone()) {
            stabilityProperty.set(Math.min(1.0, stabilityProperty.get() + STABILITY_GAIN));
            statusLabel.setText("Hold it steady...");
        } else {
            stabilityProperty.set(Math.max(0.0, stabilityProperty.get() - STABILITY_LOSS));
            statusLabel.setText("Careful! Lean back toward the green zone!");
        }

        scoreLabel.setText("Stability: " + Math.round(stabilityProperty.get() * 100) + "%");
    }

    private void updateFallPressure() {
        double offsetFromCenter = Math.abs(needleX - CENTER_X);

        if (offsetFromCenter >= FALL_LIMIT_OFFSET) {
            fallTimer = Math.min(1.0, fallTimer + FALL_TIMER_GAIN);
        } else {
            fallTimer = Math.max(0.0, fallTimer - FALL_TIMER_RECOVERY);
        }

        if (fallTimer > 0.65) {
            fallWarningLabel.setText("YOU'RE SLIPPING!");
        } else if (fallTimer > 0.35) {
            fallWarningLabel.setText("Whoa!");
        } else {
            fallWarningLabel.setText("");
        }
    }

    private void updateVisuals() {
        balanceMarker.setStartX(needleX);
        balanceMarker.setEndX(needleX);

        double offsetPercent = (needleX - CENTER_X) / MAX_NEEDLE_OFFSET;

        /*
         * This is the big visual trick:
         * the farther the needle is from center, the more the person rotates.
         */
        playerGroup.setRotate(offsetPercent * 35);

        /*
         * The rock tilts slightly in the opposite direction.
         */
        rockGroup.setRotate(offsetPercent * 8);

        /*
         * Small horizontal movement makes the player look like they are losing footing.
         */
        playerGroup.setTranslateX(offsetPercent * 22);

        /*
         * If the player is close to falling, sink/slide the body slightly.
         */
        playerGroup.setTranslateY(fallTimer * 18);
    }

    private boolean isInSafeZone() {
        double safeStart = safeZone.getX();
        double safeEnd = safeStart + safeZone.getWidth();

        return needleX >= safeStart && needleX <= safeEnd;
    }

    private void updateSafeZone() {
        double safeWidth = Math.max(
                MIN_SAFE_ZONE_WIDTH,
                START_SAFE_ZONE_WIDTH - ((rockNumber - 1) * 8)
        );

        safeZone.setWidth(safeWidth);
        safeZone.setX(CENTER_X - safeWidth / 2.0);
    }

    private double getDifficulty() {
        return 1.0 + ((rockNumber - 1) * 0.18);
    }

    private void clampNeedle() {
        if (needleX < 0) {
            needleX = 0;
            needleVelocity = 0;
        }

        if (needleX > BAR_WIDTH) {
            needleX = BAR_WIDTH;
            needleVelocity = 0;
        }
    }

    private void startGame() {
        rockNumber = 1;
        running = true;
        jumping = false;
        fallen = false;

        needleX = CENTER_X;
        needleVelocity = 0;
        fallTimer = 0;
        driftDirection = Math.random() < 0.5 ? -1 : 1;
        windTimer = 0.75;

        stabilityProperty.set(0);

        startButton.setVisible(false);
        rockLabel.setText("Rock: " + rockNumber);
        fallWarningLabel.setText("");

        playerGroup.setVisible(true);
        updateSafeZone();
        updateVisuals();

        scene.requestFocus();
    }

    private void jumpToNextRock() {
        jumping = true;
        running = false;

        statusLabel.setText("Nice! Jumping to the next rock...");
        fallWarningLabel.setText("");

        playerGroup.setTranslateY(-45);
        playerGroup.setRotate(0);
        rockGroup.setRotate(0);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.55));
        pause.setOnFinished(event -> {
            rockNumber++;

            needleX = CENTER_X + ((Math.random() * 80) - 40);
            needleVelocity = (Math.random() * 0.7) - 0.35;
            fallTimer = 0;

            stabilityProperty.set(0);

            rockLabel.setText("Rock: " + rockNumber);
            statusLabel.setText("New rock! Find your balance!");
            scoreLabel.setText("Stability: 0%");

            updateSafeZone();

            playerGroup.setTranslateY(0);
            jumping = false;
            running = true;

            scene.requestFocus();
        });

        pause.play();
    }

    private void fallOffRock() {
        fallen = true;
        running = false;

        statusLabel.setText("You fell! Press ENTER to try again.");
        fallWarningLabel.setText("SPLASH!");

        double direction = needleX < CENTER_X ? -1 : 1;

        playerGroup.setRotate(direction * 85);
        playerGroup.setTranslateX(direction * 90);
        playerGroup.setTranslateY(75);

        startButton.setText("TRY AGAIN");
    }
}