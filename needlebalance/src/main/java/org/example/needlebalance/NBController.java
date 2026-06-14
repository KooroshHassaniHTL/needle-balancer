package org.example.needlebalance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.example.needlebalance.Model.BalanceModel;

public class NBController {

    @FXML private Rectangle safeZone;
    @FXML private Line balanceMarker;
    @FXML private Rectangle backgroundBar;

    private BalanceModel balanceModel;

    private void initialize() throws InterruptedException{
        balanceModel = new BalanceModel();

        while (true){
            wait(100);
            balanceModel.randomizePosition();

        }
    }


    public void onLeftButtonClicked(ActionEvent actionEvent) {





    }

    public void onRightButtonClicked(ActionEvent actionEvent) {
    }
}
