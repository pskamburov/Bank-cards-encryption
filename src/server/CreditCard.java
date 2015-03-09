package server;

import java.util.ArrayList;

/**
 * Credit Card is presented as a ArrayList of her codes.
 *
 * @author petar
 */
public class CreditCard {

    private ArrayList<String> creditCardCodes;

    /**
     * Create a credit card.
     */
    public CreditCard() {
        creditCardCodes = new ArrayList<>();
    }

    /**
     * Get codes of the credit card.
     *
     * @return ArrayList
     */
    public ArrayList<String> getCreditCardCodes() {
        return new ArrayList<>(creditCardCodes);
    }

    /**
     * Add a new code to the card.
     *
     * @param code
     */
    public void addCreditCardCode(String code) {
        creditCardCodes.add(code);
    }

    /**
     * Get the count of credit card codes.
     *
     * @return number of codes.
     */
    public int getSize() {
        return creditCardCodes.size();
    }

    @Override
    public String toString() {
        String formatMessage = String.format("Credit card codes:%s", creditCardCodes.toString());
        return formatMessage;
    }

}
