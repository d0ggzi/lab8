import commands.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import commands.Command;
import commands.HelpCmd;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//public void read(SelectionKey key) throws IOException{
//        DatagramChannel channel = (DatagramChannel) key.channel();
//        ByteBuffer buf = ByteBuffer.allocate(100);
//        InetSocketAddress socketAddress = (InetSocketAddress) channel.receive(buf);
//        System.out.println("client ip and port:"+socketAddress.getHostString()+","+socketAddress.getPort());
//        byte[] data = buf.array();
//        String msg = new String(data).trim();
//        System.out.println("message come from client:"+msg);
//        channel.send(ByteBuffer.wrap(new String("Hello client!").getBytes()),socketAddress);
//        channel.close();
//        }

public class ServerReceiver extends RecursiveAction{
    private static DatagramPacket datagramPack;
    private static final Logger logger = LogManager.getLogger(ServerReceiver.class);
    private SelectionKey key;
    private CollectionManager collectionManager;

    public ServerReceiver(SelectionKey key, CollectionManager collectionManager){
        this.key = key;
        this.collectionManager = collectionManager;
    }

    @Override
    public void compute() {
        try{
            DatagramChannel channel = (DatagramChannel) key.channel();
            ByteBuffer buf = ByteBuffer.allocate(65536);
            InetSocketAddress socketAddress = (InetSocketAddress) channel.receive(buf);
            byte[] data = buf.array();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            ClientMessage clientMessage = (ClientMessage) ois.readObject();
            logger.info("Пришло сообщение от клиента: ");
            System.out.println(clientMessage.toString());
            ExecutorService executorService = Executors.newCachedThreadPool();
            collectionManager.setData(channel, socketAddress, clientMessage);
            executorService.execute(collectionManager);
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }



    }

}


