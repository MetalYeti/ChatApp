package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class StartServer {
    public static void main(String[] args) {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Server();
    }
}
