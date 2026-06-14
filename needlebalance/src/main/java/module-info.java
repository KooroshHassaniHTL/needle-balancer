module org.example.needlebalance {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.needlebalance to javafx.fxml;
    exports org.example.needlebalance;
}