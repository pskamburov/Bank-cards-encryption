package server;

import java.util.ArrayList;

/**
 *
 * @author petar
 */
public class CreditCardDataBase {

    private ArrayList<CreditCard> creditCards;

    /**
     * Create a database of credit cards.
     */
    public CreditCardDataBase() {
        creditCards = new ArrayList<>();
    }

    /**
     * Get the database of credit cards.
     *
     * @return
     */
    public ArrayList<CreditCard> getCreditCards() {
        // It's not good to return a reference like that, but in this case
        // this class is simulating a database so it's okay
        // It has to be that way, because this class dont have any idea about
        // tha encryption algorithms. It is just a database!
        return creditCards;
    }


    /**
     * Add a Credit card to the database.
     *
     * @param newCard
     */
    public void addCreditCard(CreditCard newCard) {
        creditCards.add(newCard);
    }

}
