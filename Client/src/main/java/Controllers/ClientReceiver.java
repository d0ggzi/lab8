package Controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import commands.ServerMessage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientReceiver {
    private DatagramChannel channel;
    private static final Logger logger = LogManager.getLogger(ClientReceiver.class);

    public ClientReceiver(DatagramChannel channel){
        this.channel = channel;
    }

    public boolean getMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(65536);
        try {
            logger.info("Клиент получает сообщение: ");
            Thread.sleep(500);
            channel.receive(buffer);
        } catch (SocketTimeoutException e) {
            logger.error("Сервер не отвечает, попробуйте позже!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try (
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array()))) {
            ServerMessage serverMessage = (ServerMessage) ois.readObject();
            String text = serverMessage.toString();
            if (text.equals("true")){
                return true;
            } if (text.equals("false")){
                return false;
            }
            showText(text);
            buffer.clear();
        } catch (StreamCorruptedException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void showText(String messageText){
        Stage window = new Stage();
        window.setMinWidth(200);
        window.setMinHeight(200);
        Text text = new Text();
        text.setText(messageText);

        System.out.println(messageText);
        window.setTitle("Ответ сервера");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(text);
        layout.setAlignment(Pos.BASELINE_LEFT);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

}
