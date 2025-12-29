package org.oms.orders_management_system;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainPage
{
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick()
    {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
