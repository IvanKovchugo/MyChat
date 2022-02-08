package client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Last100Messages {
    private static PrintWriter out;

    public static String messagesByLogin (String login) {
        return "Last100Messages/last100messages_" + login + ".txt";
    }

    public static void activate(String login) {
        try {
            out = new PrintWriter(new FileOutputStream(messagesByLogin(login), true), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disable() {
        if (out != null) {
            out.close();
        }
    }

    public static void writeLine (String msg) {
        out.println(msg);
    }

    public static String last100MessagesOfHistory (String login) {
        if (!Files.exists(Paths.get(messagesByLogin(login)))) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        try {
            List<String> messages = Files.readAllLines(Paths.get(messagesByLogin(login)));
            int first = 0;
            if (messages.size() > 100) {
                first = messages.size() - 100;
            }
            for (int i = first; i < messages.size(); i++) {
                stringBuilder.append(messages.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    return stringBuilder.toString();
    }

}
