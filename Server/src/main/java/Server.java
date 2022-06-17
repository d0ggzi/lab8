import java.io.IOException;
import java.util.Iterator;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;


public class Server {
    private static final int port = 55667;
    private static String host = "localhost";

    public static void main(String[] args) throws IOException {
        DatagramChannel serverChannel = DatagramChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(host, port));
        Selector selector;
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("=========The Server is start!===========");
        CollectionManager collectionManager = new CollectionManager();

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        new Thread(() -> {
            while(true){
                System.out.println("Ждем данных с клиента: ");
                try {
                    selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Iterator ite =  selector.selectedKeys().iterator();
                while(ite.hasNext()){
                    SelectionKey key = (SelectionKey)ite.next();
                    ite.remove();
                    ServerReceiver serverReceiver = new ServerReceiver(key, collectionManager);
                    if (key.isReadable()) forkJoinPool.invoke(serverReceiver);
                }
            }
        }).start();
        new Thread(() -> {
            Scanner scan = new Scanner(System.in);
            while (true){
                if (scan.hasNext()){
                    String[] userCommand = scan.nextLine().trim().split(" ", 2);
                    if (userCommand[0].equals("save")){
                        collectionManager.save();
                    }
                    else if (userCommand[0].equals("exit")){
                        System.out.println("Завершение программы...");
                        System.exit(1);
                    }
                    else {
                        System.out.println("Неизвестная команда, попробуйте снова.");
                    }
                }
            }
        }).start();



    }

}
