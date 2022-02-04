package server;

import service.ServiceMessages;

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
        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключиться к БД");
        }
        authServise = new DatabaseSql();
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
            SQLHandler.disconnect();
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
        broadcastClientList();
    }

    public void removeClient (ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public void broadcastMessage(ClientHandler sender, String msg) {
        String message = String.format("[%s]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMassage(message);
        }
    }

    public void privateMessage(ClientHandler sender, String toWhom, String msg) {
        String message = String.format("from: [%s] to: [%s]: %s", sender.getNickname(), toWhom, msg);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(toWhom)) {
                c.sendMassage(message);
                if (!sender.getNickname().equals(toWhom)) {
                    sender.sendMassage(message);
                }
                return;
            }
        }
        sender.sendMassage("not found user: " + toWhom);
    }

    public boolean isLoginAuthenticated (String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder stringBuilder = new StringBuilder(ServiceMessages.CLIENTLIST);
        for (ClientHandler c : clients) {
            stringBuilder.append(" ").append(c.getNickname());
        }

        String message = stringBuilder.toString();

        for (ClientHandler c : clients) {
            c.sendMassage(message);
        }
    }

    public AuthServise getAuthService() {
        return authServise;
    }

}
