package server;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author petar
 */
public class GraphicServer {

    private static JFrame frame;
    private static JTextArea displayArea;
    private final ThreadPooledServer[] servers;

    public GraphicServer(ThreadPooledServer... servers) {
        this.servers = servers;

    }

    public static void main(String[] args) {

        String USERSDATA_FILENAME = "users.xml";
        String CARDSDATA_FILENAME = "cards.xml";
        int STANDART_OFFSET = 5;//offset for the algorithm

        UserManager.initialize(USERSDATA_FILENAME);
        Algorithm algorithm = new Algorithm(STANDART_OFFSET);
        //choose algorithm
//        CryptAlgorithm substitutionCipher = algorithm.substitutionCipher();
        CryptAlgorithm railFenceCipher = algorithm.railFenceCipher();
        CreditCardManager.initialize(CARDSDATA_FILENAME, railFenceCipher);

        ThreadPooledServer server = new ThreadPooledServer(9000);
        ThreadPooledServer server2 = new ThreadPooledServer(9001);

        GraphicServer graphicServer = new GraphicServer(server, server2);
        System.out.println("Server Started");
        graphicServer.startServer();

    }

    /**
     * Display a message to the "logger".
     *
     * @param messageToDisplay
     */
    public static void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(messageToDisplay);
        });
    }

    /**
     * Start all ThreadPooled servers
     */
    public void startServer() {
        BuildGUI();
        for (ThreadPooledServer server : servers) {
            new Thread(server).start();
        }
    }

    /**
     * Draw all panels - Logger panel and the panel that save cards sorted in a
     * file
     */
    private void BuildGUI() {
        frame = new JFrame("Bank Credit Cards");
        frame.setSize(800, 600);
        //Close connection properly when X is clicked
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                frame.dispose();
                for (ThreadPooledServer server : servers) {
                    server.stop();
                }
                System.exit(0);
            }
        });

        frame.setLayout(new GridBagLayout());
        drawSortingInFilePanel();
        drawLogPanel();
//        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Draw the logger panel. A TextArea to display all users actions.
     */
    private void drawLogPanel() {
        // draw credit card panel
        JPanel pnlLogger = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        pnlLogger.setBorder(BorderFactory.createTitledBorder("Logger"));

        constraints.anchor = GridBagConstraints.CENTER;
        displayArea = new JTextArea(10, 35);
        displayArea.setEditable(false);
        pnlLogger.add(new JScrollPane(displayArea));

        constraints.gridx = 0;
        constraints.gridy = 0;
        frame.add(pnlLogger, constraints);

    }

    /**
     * Draw the panel to save cards sorted in a file. Two buttons - one to sort
     * based on card number, and the other to sort based on card codes.
     */
    private void drawSortingInFilePanel() {
        JPanel pnlSortingInFile = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        pnlSortingInFile.setBorder(BorderFactory.createTitledBorder("Sorting"));
        JLabel lblFileName = new JLabel("Please enter a file name:");
        JTextField txtFileName = new JTextField(12);
        JButton btnSortByNumbers = new JButton("Sort by credit card numbers");
        btnSortByNumbers.addActionListener((ActionEvent ae) -> {
            if (CreditCardManager
                    .saveCardsInFileSortedByNumber(
                            txtFileName.getText())) {
                String message = String.format(
                        "Credit cards saved in [%s] sorted by number\n",
                        txtFileName.getText());
                JOptionPane.showMessageDialog(frame, message,
                        "Successful", JOptionPane.INFORMATION_MESSAGE);
                displayMessage(message);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Invalid file!",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnSortByCodes = new JButton("Sort by credit card codes");
        btnSortByCodes.addActionListener((ActionEvent ae) -> {
            if (CreditCardManager
                    .saveCardsInFileSortedByCode(
                            txtFileName.getText())) {
                String message = String.format(
                        "Credit cards saved in [%s] sorted by code\n",
                        txtFileName.getText());
                JOptionPane.showMessageDialog(frame, message,
                        "Successful", JOptionPane.INFORMATION_MESSAGE);
                displayMessage(message);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Invalid file!",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 0;
        pnlSortingInFile.add(lblFileName, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        pnlSortingInFile.add(txtFileName, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        pnlSortingInFile.add(btnSortByNumbers, constraints);
        constraints.gridx = 1;
        constraints.gridy = 1;
        pnlSortingInFile.add(btnSortByCodes, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        frame.add(pnlSortingInFile, constraints);

    }

}
