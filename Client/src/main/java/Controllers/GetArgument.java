package Controllers;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GetArgument {
    static String answer;

    public static String getArg(String messageText, String messageBtn){
        Stage window = new Stage();
        Label label = new Label();
        label.setText(messageText);
        TextField textField = new javafx.scene.control.TextField();

        Button send = new Button(messageBtn);

        send.setOnAction(e -> {
            answer = textField.getText();
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, textField, send);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        return answer;
    }
}
