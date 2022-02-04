package server;

import service.ServiceMessages;

import javax.imageio.IIOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);


                    // authy
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals(ServiceMessages.END)) {
                            sendMassage(ServiceMessages.END);
                            break;
                        }
                        if (str.startsWith(ServiceMessages.AUTH)) {
                            String[] token = str.split(" ", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNick != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    authenticated = true;
                                    nickname = newNick;
                                    sendMassage(ServiceMessages.AUTH_OK + " " + nickname);
                                    server.subscribe(this);
                                    socket.setSoTimeout(0);
                                    System.out.println("Client: " + nickname + " authenticated");
                                    break;
                                } else {
                                    sendMassage("Пользователь с таким логином уже зашел в чат!");
                                }
                            } else {
                                sendMassage("Неверный логин/пароль");
                            }
                        }

                        if (str.startsWith(ServiceMessages.REG)) {
                            String[] token = str.split(" ", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            if (server.getAuthService()
                                    .registration(token[1], token[2], token[3])) {
                                sendMassage(ServiceMessages.REG_OK);
                            } else {
                                sendMassage(ServiceMessages.REG_NO);
                            }
                        }
                    }

                    // work
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith(ServiceMessages.SLASH)) {
                            if (str.equals(ServiceMessages.END)) {
                                sendMassage(ServiceMessages.END);
                                break;
                            }
                            if (str.startsWith(ServiceMessages.W)) {
                                String[] privatToken = str.split(" ", 3);
                                if (privatToken.length < 3) {
                                    continue;
                                }
                                server.privateMessage(this, privatToken[1], privatToken[2]);
                            }
                        } else {
                            server.broadcastMessage(this, str);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMassage(ServiceMessages.END);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    server.removeClient(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        } catch (IIOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMassage(String massage) {
        try {
            out.writeUTF(massage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
