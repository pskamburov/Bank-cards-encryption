package server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClientHandlerRunnable is responsible for the communication between client and
 * server. Each client is "handled" in a different thread by this class. That's
 * why it implements run method.
 *
 * @author petar
 */
public class ClientHandlerRunnable implements Runnable {

    private Socket clientSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private User currentUser;

    /**
     * Constructor - create socket and initialize streams.
     *
     * @param clientSocket
     */
    public ClientHandlerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            //if the socket input/output has been shutdown
            ex.printStackTrace();
        }
    }

    /**
     * Provides the communication. Waits for user to log in system and then
     * according to his requests execute actions. These actions are reported to
     * the server.
     *
     */
    @Override
    public void run() {
        try {
            if (logInSystem()) {
                String action;
                do {
                    action = (String) input.readObject();
                    switch (action) {
                        case "NEW USER":
                            createNewUser();
                            break;
                        case "ENCRYPT":
                            encryptCard();
                            break;
                        case "DECRYPT":
                            decryptCard();
                            break;
                    }
                } while (!action.equals("END"));
                String message = String.format("[%s] closed the connection!\n", currentUser);
                GraphicServer.displayMessage(message);
                closeConnection();
            } else {
                String message = String.format(
                        "User failed to log in, closing the connection!\n");
                GraphicServer.displayMessage(message);
                closeConnection();
            }
        } catch (IOException e) {
            //client is sending unexpected request or
            //unexpectedly terminated the connection
            //should never happen in normal circumstances
            GraphicServer.displayMessage(
                    String.format("[%s]Unexpected action!\n", currentUser));
            try {
                closeConnection();
            } catch (IOException ex) {
                // something is wrong with the socket, cannot close it properly
                ex.printStackTrace();
            }

        } catch (ClassNotFoundException ex) {
            //can't find the proper class to load.
            ex.printStackTrace();
        }
    }

    /**
     * Set current User to a given User.
     *
     * @param currentUser
     */
    private void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Accepts a username and password from client and check the database for
     * the specified User. Sends back to client the User(with permissions) in
     * form of XML(using XStream).
     *
     * @return true if successfully logged in, false if user is incorrect
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private boolean logInSystem() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        String message = String.format("[%s] trying to log in\n", username);
        GraphicServer.displayMessage(message);
        if (UserManager.getUser(username, password) == null) {
            output.writeObject("NOT LOGGED IN");
            output.flush();
            return false;
        }
        output.writeObject("LOGGED IN");
        setCurrentUser(UserManager.getUser(username, password));
        String succMessage = String.format("[%s] logged in successfully!\n",
                currentUser);
        GraphicServer.displayMessage(succMessage);
        XStream xstream = new XStream(new DomDriver());
        output.writeObject(xstream.toXML(currentUser));
        output.flush();
        return true;
    }

    /**
     * Add a new user to the database. User info(name, password, permissions) is
     * received from the client in form of XML(using XStream). Current User
     * should be administrator in order to use this method.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private synchronized void createNewUser() throws IOException, ClassNotFoundException {
        String userXML = (String) input.readObject();
        XStream xstream = new XStream(new DomDriver());
        User newUser = (User) xstream.fromXML(userXML);
        if (currentUser.getAccess().isAdministrator()
                && UserManager.createNewUser(newUser)) {
            output.writeObject("SUCCESSFUL");
            String message = String.format(
                    "[%s] created user [%s] successfully!\n",
                    currentUser, newUser);
            GraphicServer.displayMessage(message);
        } else {
            output.writeObject("NOT SUCCESSFUL");
            String message = String.format(
                    "User [%s] NOT created!\n", newUser);
            GraphicServer.displayMessage(message);
        }
    }

    /**
     * Encrypt credit card number. Receive number from client, and sends back to
     * him the code, or appropriate response if something goes wrong.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private synchronized void encryptCard() throws IOException, ClassNotFoundException {
        String cardNumber = (String) input.readObject();
        if (!currentUser.getAccess().isAbleToEncrypt()) {
            output.writeObject("NOACCESS");
        } else if (!CreditCardManager.isValidNumber(cardNumber)) {
            output.writeObject("INVALID");
        } else if (!CreditCardManager.isLuhnAlgorithmValid(cardNumber)) {
            output.writeObject("LUHN");
        } else if (!CreditCardManager.cardCodesSizeSmallerThanTwelve(cardNumber)) {
            output.writeObject("TOOMANY");
        } else {
            String creditCardCode = CreditCardManager.encrypt(cardNumber);
            output.writeObject("OK");
            output.writeObject(creditCardCode);
            String message = String.format(
                    "[%s] encrypted creditcard with number [%s] successfully!\n",
                    currentUser, cardNumber);
            GraphicServer.displayMessage(message);
        }

    }

    /**
     * Decrypt credit card number. Receive number from client, and sends back to
     * him the number of card, or appropriate response if something goes wrong.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void decryptCard() throws IOException, ClassNotFoundException {
        String cardCode = (String) input.readObject();
        if (!currentUser.getAccess().isAbleToRequestCard()) {
            output.writeObject("NOACCESS");
        } else if (!CreditCardManager.cardExistsByCode(cardCode)) {
            output.writeObject("NOCARD");
        } else {
            String creditCardNumber = CreditCardManager.decrypt(cardCode);
            output.writeObject("OK");
            output.writeObject(creditCardNumber);
            String message = String.format(
                    "[%s] decrypted creditcard with code [%s] successfully!\n",
                    currentUser, cardCode);
            GraphicServer.displayMessage(message);

        }
    }

    /**
     * Close the connection with the client.
     *
     * @throws IOException
     */
    private void closeConnection() throws IOException {
        input.close();
        output.close();
        clientSocket.close();
    }

}
