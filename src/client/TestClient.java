package client;

import java.io.IOException;

/**
 *
 * @author petar
 */
public class TestClient {

    public static void main(String[] args) throws ClassNotFoundException {

        Client testClient = new Client();

        try {
            testClient.login();
        } catch (IOException ex) {
            testClient.showTerminatedConnectionError();
        }
        testClient.BuildGUI();
//Luhn valid numbers:
//3563 9601 1211 7435
//3563 9601 1214 7382
//5563 9601 1254 7412
//4563 9601 1254 7415
    }
}
