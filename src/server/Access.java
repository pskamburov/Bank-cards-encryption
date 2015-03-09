package server;


/**
 *
 * @author petar
 */
public class Access {

    private boolean administrator;
    private boolean ableToEncrypt;
    private boolean ableToRequestCard;

    /**
     * create access - is administrator, is able to encrypt, is able to request
     * code.
     *
     * @param administrator
     * @param ableToEncrypt
     * @param ableToRequestCard
     */
    public Access(boolean administrator,
            boolean ableToEncrypt,
            boolean ableToRequestCard) {
        setAdministrator(administrator);
        setAbleToEncrypt(ableToEncrypt);
        setAbleToRequestCard(ableToRequestCard);
    }

    /**
     * Check administrator permission.
     *
     * @return True if is administrator, false - otherwise.
     */
    public boolean isAdministrator() {
        return administrator;
    }

    /**
     * Set administrator permission.
     *
     * @param administrator
     */
    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    /**
     * Check encryption permission.
     *
     * @return True if is able to encrypt, false - otherwise.
     */
    public boolean isAbleToEncrypt() {
        return ableToEncrypt;
    }

    /**
     * Set encryption permission.
     *
     * @param ableToEncrypt
     */
    public void setAbleToEncrypt(boolean ableToEncrypt) {
        this.ableToEncrypt = ableToEncrypt;
    }

    /**
     * Check decryption permission.
     *
     * @return True if is able to request card number, false - otherwise.
     */
    public boolean isAbleToRequestCard() {
        return ableToRequestCard;
    }

    /**
     * Set permission to request card number(decrypt).
     *
     * @param ableToRequestCard
     */
    public void setAbleToRequestCard(boolean ableToRequestCard) {
        this.ableToRequestCard = ableToRequestCard;
    }

}
