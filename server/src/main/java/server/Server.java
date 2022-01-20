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
    private AuthServise authServise;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authServise = new SimpleAuthyService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server have been started!");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected!" + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
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

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void removeClient (ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcastMessage(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMassage(message);
        }
    }

    public void privateMessage(ClientHandler sender, String toWhom, String msg) {
        String privateMessageformat = String.format("[ %s ] [ s% ] : %s", sender.getNickname(), toWhom ,msg );
        toWhom = sender.getNickname();
        for (ClientHandler c : clients) {
            c.sendMassage(privateMessageformat);
        }
    }

    public AuthServise getAuthService() {
        return authServise;
    }
}
