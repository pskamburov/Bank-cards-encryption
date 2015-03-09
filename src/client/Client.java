package client;

import server.Access;
import server.User;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author petar
 */
public class Client {

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private User user;
    private JFrame frame;
    private JTextField txtInfo;

    public Client() {
    }

    /**
     * Log in form, using Swing JOptionPane windows. Show form with 2
     * fields(username and password), connect to server, log with them and set
     * current user.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void login() throws IOException, ClassNotFoundException {
        JTextField txtFieldUsername = new JTextField();
        JTextField txtFieldPassword = new JPasswordField();
        Object[] message = {
            "Username:", txtFieldUsername,
            "Password:", txtFieldPassword,};
        while (true) {
            int option = JOptionPane.showConfirmDialog(null,
                    message, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String username = txtFieldUsername.getText();
                String password = txtFieldPassword.getText();
//for testing purposes
//                String username = "Admin";
//                String password = "12345";
                if (!isValidUsername(username)) {
                    JOptionPane.showMessageDialog(null,
                            "*username must be 4 to 26 letters\n"
                            + "*username can contain Upper/Lower letters and -",
                            "Invalid username", JOptionPane.ERROR_MESSAGE);
                } else if (!isValidPassword(password)) {
                    JOptionPane.showMessageDialog(null,
                            "*password must be 5 to 15 letters\n"
                            + "*password can contain Upper/Lower letters and numbers",
                            "Invalid password", JOptionPane.ERROR_MESSAGE);
                } else {
                    connect("127.0.0.1", 9000);
                    if (loggedIn(username, password)) {
                        setUser();
                        JOptionPane.showMessageDialog(null,
                                "Successfully logged in",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Wrong username or password",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        endConnection();
                    }
                }
            } else {
                if (socket != null) {
                    endConnection();
                }
                System.out.println("Need to log in!");
                System.exit(0);
            }
        }

    }

    /**
     * Draw all panels - Credit card panel, Information panel and Create new
     * user panel.
     */
    public void BuildGUI() {
        frame = new JFrame("Bank Credit Cards");
        frame.setSize(800, 600);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Close connection properly when X is clicked
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                endConnection();
                frame.dispose();
                System.exit(0);
            }
        });
        frame.setLayout(new GridBagLayout());

        drawCreditCardPanel();
        drawInfoPanel();
        if (user.getAccess().isAdministrator()) {
            drawAdminPanel();
        }
//        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Show message to client that the server is offline and terminate program.
     */
    public void showTerminatedConnectionError() {
        JOptionPane.showMessageDialog(null,
                "Server is offline!",
                "Terminated connection", JOptionPane.ERROR_MESSAGE);
        //close socket if it is opened
        if (socket != null) {
            endConnection();
        }
        System.exit(0);
    }

    /**
     * check if string is a valid username. Starting with a upper/lower case
     * letter and username can contain "-" and " ". Minimum size of the
     * username: 4 symbols, maximum: 26 symbols.
     *
     * @param username
     * @return
     */
    private static boolean isValidUsername(String username) {
        final String USERNAME_PATTERN = "^[a-zA-Z]{1}[ a-zA-Z\\-]{3,25}$";
        return username.matches(USERNAME_PATTERN);
    }

    /**
     * check if string is a valid password. Password can contain upper/lower
     * letters and numbers. Minimum size: 5 symbols, maximum: 16 symbols.
     *
     * @param password
     * @return
     */
    private static boolean isValidPassword(String password) {
        final String PASSWORD_PATTERN = "^[a-zA-Z0-9]{5,15}$";
        return password.matches(PASSWORD_PATTERN);
    }

    /**
     * Connect to a specified host and port.
     *
     * @param host
     * @param port
     * @throws IOException
     */
    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Set current user(name, password, access), receive info from server via
     * XStream.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void setUser() throws IOException, ClassNotFoundException {
        String userXML = (String) input.readObject();
        XStream xstream = new XStream(new DomDriver());
        user = (User) xstream.fromXML(userXML);
    }

    /**
     * Trying to log in system with username and password.
     *
     * @param username
     * @param password
     * @return true if logged in, false - otherwise.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private boolean loggedIn(String username, String password) throws IOException, ClassNotFoundException {
        output.writeObject(username);
        output.writeObject(password);
        output.flush();
        String line = (String) input.readObject();
        return line.equals("LOGGED IN");
    }

    /**
     * Try to create new user. true - if successfully created user false -
     * disallowed(invalid symbols or already existing username) username or
     * password.
     *
     * @param username
     * @param password
     * @param access
     * @return
     */
    private boolean createNewUser(String username,
            String password, Access access) {
        try {
            if (!isValidUsername(username)) {
                displayMessage("Invalid username");
            } else if (!isValidPassword(password)) {
                displayMessage("Invalid password");
            } else {
                output.writeObject("NEW USER");
                User newUser = new User(username, password, access);
                XStream xstream = new XStream(new DomDriver());
                output.writeObject(xstream.toXML(newUser));
                output.flush();
                String line = (String) input.readObject();
                if (line.equals("SUCCESSFUL")) {
                    String message
                            = String.format("Successfully created user %s",
                                    username);
                    displayMessage(message);
                } else {
                    String message
                            = String.format("User %s already exists ",
                                    username);
                    displayMessage(message);
                }
                return line.equals("SUCCESSFUL");
            }
        } catch (IOException | ClassNotFoundException ex) {
            showTerminatedConnectionError();
        }
        return false;

    }

    /**
     * Encrypt a given credit card number and display the code. Handles
     * different responses from the server for incorrect situations.
     *
     * @param number
     */
    private void encryptCard(String number) {
        try {
            output.writeObject("ENCRYPT");
            output.writeObject(number);
            String response = (String) input.readObject();
            switch (response) {
                case "NOACCESS":
                    displayMessage("You don't have permission!");
                    break;
                case "INVALID":
                    displayMessage("Invalid credit card number!");
                    break;
                case "LUHN":
                    displayMessage("Credit card number is not Luhn algorithm valid!");
                    break;
                case "TOOMANY":
                    displayMessage("Can't encrypt more than 12 times!");
                    break;
                case "OK":
                    displayMessage((String) input.readObject());
                    break;
            }
        } catch (IOException | ClassNotFoundException ex) {
            showTerminatedConnectionError();
        }

    }

    /**
     * Decrypt a given credit card code and display the number. Handles
     * different responses from the server for invalid situations.
     *
     * @param code credit card code
     */
    private void decryptCard(String code) {
        try {
            output.writeObject("DECRYPT");
            output.writeObject(code);
            String response = (String) input.readObject();
            switch (response) {
                case "NOACCESS":
                    displayMessage("You don't have permission!");
                    break;
                case "INVALID":
                    displayMessage("Invalid credit card number!");
                    break;
                case "NOCARD":
                    displayMessage("No such card!");
                    break;
                case "OK":
                    displayMessage((String) input.readObject());
                    break;
            }
        } catch (IOException | ClassNotFoundException ex) {
            showTerminatedConnectionError();
        }

    }

    /**
     * End connection with the server properly.
     *
     * @throws IOException
     */
    private void endConnection() {
        try {
            output.writeObject("END");
            input.close();
            output.close();
            socket.close();
        } catch (IOException ex) {
            //server already closed the connection, so don't do anything
        }

    }

    /**
     * Display message on JTextField(txtInfo).
     *
     * @param message
     */
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            txtInfo.setText(message);
        });
    }

    /**
     * Draw credit card panel. Two labels(to show information), two
     * JtextFields(to get credit card number and code) and two buttons(to
     * encrypt a card or to get number of a card).
     */
    private void drawCreditCardPanel() {
        // draw credit card panel
        JPanel pnlcreditCard = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        pnlcreditCard.setBorder(BorderFactory.createTitledBorder("Credit card panel"));

        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(5, 5, 5, 5);

        if (user.getAccess().isAbleToEncrypt()) {
            JLabel lblCreditCardNumber = new JLabel("Number:");
            JTextField txtNumber = new JTextField(12);
            JButton btnGetCode = new JButton("Encrypt");
            btnGetCode.addActionListener((ActionEvent ae) -> {
                encryptCard(txtNumber.getText());
            });
            constraints.gridx = 0;
            constraints.gridy = 0;
            pnlcreditCard.add(lblCreditCardNumber, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            pnlcreditCard.add(txtNumber, constraints);
            constraints.gridx = 2;
            constraints.gridy = 0;
            pnlcreditCard.add(btnGetCode, constraints);
        }
        if (user.getAccess().isAbleToRequestCard()) {
            JLabel lblCreditCardCode = new JLabel("Code:");
            JTextField txtCode = new JTextField(12);
            JButton btnGetNumber = new JButton("Get number");
            btnGetNumber.addActionListener((ActionEvent ae) -> {
                decryptCard(txtCode.getText());
            });
            constraints.gridx = 0;
            constraints.gridy = 1;
            pnlcreditCard.add(lblCreditCardCode, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            pnlcreditCard.add(txtCode, constraints);
            constraints.gridx = 2;
            constraints.gridy = 1;
            pnlcreditCard.add(btnGetNumber, constraints);
        }

        if (user.getAccess().isAbleToEncrypt()
                || user.getAccess().isAbleToRequestCard()) {
            constraints.gridx = 0;
            constraints.gridy = 0;
            frame.add(pnlcreditCard, constraints);
        }
    }

    /**
     * Draw information panel. One label(to show different kind of information
     * to the user).
     */
    private void drawInfoPanel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel lblInfo = new JLabel();
        lblInfo.setPreferredSize(new Dimension(280, 30));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setText("Hello, " + user.getUsername());
        pnlInfo.add(lblInfo);
        txtInfo = new JTextField();
        txtInfo.setHorizontalAlignment(SwingConstants.CENTER);
        txtInfo.setEditable(false);
        pnlInfo.add(txtInfo);

        constraints.gridx = 0;
        constraints.gridy = 1;
        frame.add(pnlInfo, constraints);
    }

    /**
     * Draw the panel for creating a new user.Two labels(to show information), 3
     * CheckBoxes(to set new user access), two JTextFields(to input username and
     * password), Button(to create the new user)
     */
    private void drawAdminPanel() {
//Draw create new user panel
        JPanel pnlAdmin = new JPanel(new GridLayout(0, 2));
        GridBagConstraints constraints = new GridBagConstraints();
        pnlAdmin.setBorder(BorderFactory.createTitledBorder("Create new user"));
        JLabel lblUsername = new JLabel("Username:  ");
        JTextField txtUsername = new JTextField(13);
        lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlAdmin.add(lblUsername);
        pnlAdmin.add(txtUsername);
        JLabel lblPassword = new JLabel("Password:  ");
        JPasswordField txtPassword = new JPasswordField(13);
        lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlAdmin.add(lblPassword);
        pnlAdmin.add(txtPassword);
        JCheckBox chbIsAbleToEncrypt = new JCheckBox("Able to encrypt", true);
        JCheckBox chbIsAbleToRequest = new JCheckBox("Able to request number", true);
        JCheckBox chbIsAdministrator = new JCheckBox("Administrator");
        pnlAdmin.add(chbIsAbleToEncrypt);
        pnlAdmin.add(chbIsAdministrator);
        pnlAdmin.add(chbIsAbleToRequest);
        JButton btnCreateUser = new JButton("Create user");
        btnCreateUser.addActionListener((ActionEvent ae) -> {
            createNewUser(txtUsername.getText(),
                    new String(txtPassword.getPassword()),
                    new Access(chbIsAdministrator.isSelected(),
                            chbIsAbleToEncrypt.isSelected(),
                            chbIsAbleToRequest.isSelected()));
        });
        pnlAdmin.add(btnCreateUser);
        constraints.gridx = 0;
        constraints.gridy = 2;
        frame.add(pnlAdmin, constraints);
    }

}
