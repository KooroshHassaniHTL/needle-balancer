package org.example.needlebalance.Model;

public class BalanceModel {
    private double currentPosition = 0.5;
    private double safeZoneStart = 0.4;
    private double safeZoneEnd = 0.6;


    public double getCurrentPosition() { return currentPosition; }

    public void randomizePosition() {

        currentPosition = Math.random();
    }

    public boolean isSafeZone() {
        return currentPosition >= safeZoneStart && currentPosition <= safeZoneEnd;
    }

    public void moveLeft() {
        currentPosition -= 0.01;
    }
    public void moveRight() {
        currentPosition += 0.01;
    }
}
