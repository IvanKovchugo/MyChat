package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8253;

    private List<ClientHandler> clients;

    public Server() {
        clients = new CopyOnWriteArrayList<>();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server have been started!");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected!" + socket.getRemoteSocketAddress());
                clients.add(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server closed");
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public  void  broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMassage(message);
        }
    }
}
