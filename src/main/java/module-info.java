module com.parzival.a1q2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.parzival.a1q2 to javafx.fxml;
    exports com.parzival.a1q2;
}