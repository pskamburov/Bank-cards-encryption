package server;

import java.util.ArrayList;

/**
 *
 * @author petar
 */
public class UserDataBase {

    private ArrayList<User> users;

    /**
     * Creates an empty database of users.
     */
    public UserDataBase() {
        users = new ArrayList<>();
    }

    /**
     * Get database.
     *
     * @return list of users
     */
    public ArrayList<User> getUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Get user with a specified username and password from the database.
     *
     * @param username
     * @param password
     * @return User, or null if user don't exists
     */
    public User getUser(String username, String password) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username)
                        && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a new User
     *
     * @param newUser
     */
    public void addUser(User newUser) {
        users.add(newUser);
    }
}
