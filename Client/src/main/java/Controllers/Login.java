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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Login implements Initializable {
    private Parent root;
    private Stage stage;
    private Scene scene;
    private String login;
    private String password;
    private boolean log_or_reg = false;
    private ClientSender clientSender;
    private ClientReceiver clientReceiver;
    private ResourceBundle resources;
    private String[] languages = {"RU", "EN", "FI", "IT"};

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Text errorText;

    @FXML
    private ChoiceBox<String> languageChoice;


    public void setClient(ClientReceiver clientReceiver, ClientSender clientSender) {
        this.clientReceiver = clientReceiver;
        this.clientSender = clientSender;
    }


    public void login(ActionEvent event) throws IOException {
        this.login = usernameInput.getText();
        this.password = passwordInput.getText();
        AuthData authData = new AuthData("login", this.login, this.password);
        ClientMessage userData = new ClientMessage(new AuthCmd(), authData);
        clientSender.sendMessage(userData);
        log_or_reg = clientReceiver.getMessage();
        if (!log_or_reg){
            errorText.setText(resources.getString("errorTextLogin"));
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

    public void goRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"), resources);
        root = loader.load();

        Register register = loader.getController();
        register.setClient(clientReceiver, clientSender);

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void changeLanguage(ActionEvent event){
        String lang = languageChoice.getValue();
        this.resources = ResourceBundle.getBundle("bundles.Locale", new Locale(lang));
        reload(event);
    }

    public void reload(ActionEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"), resources);
            root = loader.load();

            Login login = loader.getController();
            login.setClient(clientReceiver, clientSender);

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        languageChoice.getItems().addAll(languages);
        languageChoice.setOnAction(this::changeLanguage);
    }
}
