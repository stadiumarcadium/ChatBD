package Chat;

import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class ServerApp {
    private static final int DEFAULT_PORT = 8186;

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/main/resources/logs/configs/log4j.properties");
        try {
            new MyServer(DEFAULT_PORT).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
