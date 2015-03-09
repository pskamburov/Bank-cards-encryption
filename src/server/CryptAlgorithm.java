package server;

/**
 *
 * @author petar
 */
public interface CryptAlgorithm {

    /**
     * Return the default offset of the algorithm.
     *
     * @return
     */
    int getStandartOffset();

    /**
     * Encrypt credit card number with given offset.
     *
     * @param creditCardNumber
     * @param offset
     * @return
     */
    String encrypt(String creditCardNumber, int offset);

    /**
     * Decrypt credit card number with given offset.
     *
     * @param creditCardCode
     * @param offset
     * @return
     */
    String decrypt(String creditCardCode, int offset);

}
