package server;

import server.Access;


/**
 *
 * @author petar
 */
public class User {

    private String username;
    private String password;
    private Access access;

    /**
     * Constructor using username,password and access
     *
     * @param username
     * @param password
     * @param access
     */
    public User(String username, String password, Access access) {
        setUsername(username);
        setPassword(password);
        setAccess(access);
    }

    /**
     * get username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * get password
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * set password
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * get access
     *
     * @return
     */
    public Access getAccess() {
        return access;
    }

    /**
     * set access
     *
     * @param access
     */
    public void setAccess(Access access) {
        this.access = access;
    }

    /**
     * visualize a user
     *
     * @return
     */
    @Override
    public String toString() {
        return username;
    }

}
