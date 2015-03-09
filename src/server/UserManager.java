package server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * UserManager is responsible for user database provided by a XML file. Provides
 * a lot of features to make the work with the database really easy.
 *
 * @author petar
 */
public final class UserManager {

    private static File file;
    private static UserDataBase dataBase;

    /**
     * private constructor
     */
    private UserManager() {
    }

    /**
     * Initialize UserManager's instance using a filename. File should be empty
     * or contain XML formatted Users(using XStream). If the file don't exists,
     * it will be created.
     *
     * @param filename
     */
    public static void initialize(String filename) {
        file = new File(filename);
        loadFromXML();
    }

    /**
     * Get user with a specified username and password from the database.
     *
     * @param username
     * @param password
     * @return User, or null if user don't exists
     */
    public static User getUser(String username, String password) {
        return dataBase.getUser(username, password);
    }

    /**
     * Add a new user in the database.
     *
     * @param newUser - the User to be added
     * @return false if user with that username already exists, true - otherwise
     */
    public static boolean createNewUser(User newUser) {
        if (userExists(newUser.getUsername())) {
            return false;
        }
        dataBase.addUser(newUser);
        exportToXML();
        return true;
    }

    /**
     * Loads a database of users from the file given on initialization.
     */
    private static void loadFromXML() {
        if (file.exists() && !file.isDirectory()) {
            XStream xstream = new XStream(new DomDriver());
            xstream.alias("UserDataBase", UserDataBase.class);
            xstream.alias("User", User.class);
            dataBase = (UserDataBase) xstream.fromXML(file);
        } else {
            dataBase = new UserDataBase();
        }
    }

    /**
     * Saves current database(in XML format, using XStream) to file given to the
     * constructor.
     */
    private static void exportToXML() {
        try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile())) {
            XStream xstream = new XStream(new DomDriver());
            xstream.alias("UserDataBase", UserDataBase.class);
            xstream.alias("User", User.class);
            xstream.toXML(dataBase, writer);
        } catch (FileNotFoundException ex) {
            //cannot create that file, report the exception
            ex.printStackTrace();
        }
    }

    /**
     * Check if user with given String exists in the database.
     *
     * @param username String
     * @return True - if user exists, false - otherwise.
     */
    private static boolean userExists(String username) {
        return dataBase.getUsers().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

}
