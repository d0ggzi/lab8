import commands.ServerMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;


public class ServerSender implements Serializable, Runnable {
    private ServerMessage serverMessage;
    DatagramChannel channel;
    InetSocketAddress socketAddress;
    public ServerSender(ServerMessage serverMessage, DatagramChannel channel, InetSocketAddress socketAddress) {
        this.serverMessage = serverMessage;
        this.channel = channel;
        this.socketAddress = socketAddress;
    }

    @Override
    public void run() {
        try (ByteArrayOutputStream byteStream = new
                ByteArrayOutputStream(65536);
             ObjectOutputStream os = new ObjectOutputStream(new
                     BufferedOutputStream(byteStream));) {
            os.flush();
            os.writeObject(serverMessage);
            os.close();
            byte[] sendBuf = byteStream.toByteArray();
            channel.send(ByteBuffer.wrap(sendBuf), socketAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
