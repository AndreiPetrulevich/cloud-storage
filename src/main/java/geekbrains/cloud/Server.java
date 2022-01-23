package geekbrains.cloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        var serverSocket = new ServerSocket(8190);

        System.out.println("Server started");

        try {
            while(true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverSocket.close();
    }
}
