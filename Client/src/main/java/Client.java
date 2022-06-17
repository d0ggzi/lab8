import Controllers.ClientReceiver;
import Controllers.ClientSender;
import Controllers.Login;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.channels.DatagramChannel;
import java.util.ResourceBundle;

public class Client extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try{
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            ClientSender clientSender = new ClientSender(channel);
            ClientReceiver clientReceiver = new ClientReceiver(channel);

            ResourceBundle resources = ResourceBundle.getBundle("bundles.Locale");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"), resources);
            Parent root = loader.load();

            Login login = loader.getController();
            login.setClient(clientReceiver, clientSender);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}