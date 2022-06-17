package Controllers;

import commands.AuthCmd;
import commands.AuthData;
import commands.ClientMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Register implements Initializable {
    private Parent root;
    private Stage stage;
    private Scene scene;
    private String login;
    private String password;
    private boolean log_or_reg = false;
    private ClientSender clientSender;
    private ClientReceiver clientReceiver;
    private ResourceBundle resources;

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Text errorText;


    public void setClient(ClientReceiver clientReceiver, ClientSender clientSender) {
        this.clientReceiver = clientReceiver;
        this.clientSender = clientSender;
    }


    public void register(ActionEvent event) throws IOException {
        this.login = usernameInput.getText();
        this.password = passwordInput.getText();

        AuthData authData = new AuthData("register", this.login, this.password);
        ClientMessage userData = new ClientMessage(new AuthCmd(), authData);
        clientSender.sendMessage(userData);
        log_or_reg = clientReceiver.getMessage();
        if (!log_or_reg){
            errorText.setText(resources.getString("errorTextReg"));
            errorText.setStyle("-fx-text-fill: red");
        } else{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-scene.fxml"), resources);
            root = loader.load();

            MainScene mainScene = loader.getController();
            mainScene.setData(clientReceiver, clientSender, authData);

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    public void goLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"), resources);
        root = loader.load();

        Login login = loader.getController();
        login.setClient(clientReceiver, clientSender);

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }

}
