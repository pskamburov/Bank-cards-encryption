package server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author petar
 */
public final class CreditCardManager {

    private static File file;
    private static CreditCardDataBase cardsDataBase;
    private static CryptAlgorithm algorithm;

    /**
     * private constructor
     */
    private CreditCardManager() {
    }

    /**
     * Initialize CreditCardManager's instance using a filename and a
     * CryptAlgorithm. File should be empty or contain XML formatted
     * CreditCards(using XStream). If the file don't exists, it will be created.
     *
     * @param filename
     * @param algorithm provides the encryption/decryption algorithm
     */
    public static void initialize(String filename, CryptAlgorithm algorithm) {
        CreditCardManager.algorithm = algorithm;
        file = new File(filename);
        loadFromXML();
    }

    /**
     * Check if a string is valid for a credit card number. Number can contain
     * digits and whitespace. Should start with 3,4,5,6. Minimum length 16
     * symbols, maximum 19 symbols. And LuhnAlgorithm valid!
     *
     * @param name
     * @return true if is valid, false - otherwise.
     */
    public static boolean isValidNumber(String name) {
        final String CARD_PATTERN = "^[3-6]{1}[0-9 ]{15,18}$";
        return name.matches(CARD_PATTERN);
    }

    /**
     * Check if string is LuhnAlgorihm valid.
     *
     * @param number
     * @return true if is valid, false - otherwise.
     */
    public static boolean isLuhnAlgorithmValid(String number) {
        number = number.replace(" ", "");
        int oddSum = 0, evenSum = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {
                //this is for odd digits, they are 1-indexed in the algorithm
                oddSum += digit;
            } else {
                //add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                evenSum += 2 * digit;
                if (digit >= 5) {
                    evenSum -= 9;
                }
            }
        }
        return (oddSum + evenSum) % 10 == 0;
    }

    /**
     * Save credit cards in file in table format and sorted by credit card
     * number
     *
     * @param filename
     * @return false if it can't access file, or can't create new one, true -
     * otherwise
     */
    public static boolean saveCardsInFileSortedByNumber(String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.printf("%-19s - %19s",
                    "Credit Card Code", "Credit Card Number");
            writer.println();
            ArrayList<CreditCard> cards = new ArrayList<>(
                    // sort cards by their number
                    cardsDataBase.getCreditCards().stream()
                    .sorted((CreditCard t, CreditCard t1)
                            //get number == get first code and decrypt it, using standart offset
                            -> decryptCard(t.getCreditCardCodes().get(0),
                                    algorithm.getStandartOffset())
                            .compareTo(
                                    decryptCard(t1.getCreditCardCodes().get(0),
                                            algorithm.getStandartOffset())))
                    .collect(Collectors.toList()));
            for (CreditCard card : cards) {
                String creditCardNumber = decryptCard(
                        card.getCreditCardCodes().get(0),
                        algorithm.getStandartOffset());
                for (String cardCode : card.getCreditCardCodes()) {
                    writer.printf("%-19s - %19s", cardCode, creditCardNumber);
                    writer.println();
                }
            }
            writer.flush();
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        }
    }

    /**
     * Save credit cards in file in table format and sorted by credit card codes
     *
     * @param filename
     * @return false if it can't access file, or can't create new one, true -
     * otherwise
     */
    public static boolean saveCardsInFileSortedByCode(String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.printf("%-19s - %19s",
                    "Credit Card Code", "Credit Card Number");
            writer.println();
            TreeMap<String, String> codeNumberSorted = new TreeMap<>();
            // push all <code,number> in a sorted map
            for (CreditCard card : cardsDataBase.getCreditCards()) {
                String creditCardNumber = decryptCard(
                        card.getCreditCardCodes().get(0),
                        algorithm.getStandartOffset());
                for (String code : card.getCreditCardCodes()) {
                    codeNumberSorted.put(code, creditCardNumber);
                }
            }
            for (Map.Entry<String, String> entry : codeNumberSorted.entrySet()) {
                String code = entry.getKey();
                String number = entry.getValue();
                writer.printf("%-19s - %19s", code, number);
                writer.println();
            }
            writer.flush();
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        }
    }

    /**
     * Check if credit card with a given code exists in the database.
     *
     * @param code
     * @return true if exists, false - otherwise.
     */
    public static boolean cardExistsByCode(String code) {
        return cardsDataBase.getCreditCards().stream()
                .anyMatch(card -> card.getCreditCardCodes().contains(code));
    }

    /**
     * Check if credit card with a given number is encrypted less than twelve
     * times.
     *
     * @param number
     * @return true if card size < 12, otherwize false
     */
    public static boolean cardCodesSizeSmallerThanTwelve(String number) {
        if (!cardExistsByNumber(number)) {
            return true;
        }
        return getCardByNumber(number).getSize() < 12;
    }

    /**
     * Encrypt a credit card with given number, and add it to the database.
     *
     * @param number
     * @return Code of the credit card.
     */
    public static String encrypt(String number) {
        CreditCard card = getCardByNumber(number);
        int offset = (card == null) ? 0 : card.getSize();
        offset = (offset + algorithm.getStandartOffset()) % 16;
        String cardCode = cryptCard(number, offset);
        if (card != null) {
            card.addCreditCardCode(cardCode);
        } else {
            card = new CreditCard();
            card.addCreditCardCode(cardCode);
            cardsDataBase.addCreditCard(card);
        }
        exportToXML();
        return cardCode;
    }

    /**
     * Decrypt a credit card with given code.
     *
     * @param code
     * @return Number of the credit card.
     */
    public static String decrypt(String code) {
        CreditCard card = getCardByCode(code);
        return decryptCard(card.getCreditCardCodes().get(0),
                algorithm.getStandartOffset());
    }

    /**
     * Loads a database of credit cards from the file given to the constructor.
     */
    private static void loadFromXML() {
        if (file.exists() && !file.isDirectory()) {
            XStream xstream = new XStream(new DomDriver());
            xstream
                    .alias("CreditCardDataBase", CreditCardDataBase.class
                    );
            xstream.alias(
                    "CreditCard", CreditCard.class
            );
            cardsDataBase = (CreditCardDataBase) xstream.fromXML(file);
        } else {
            cardsDataBase = new CreditCardDataBase();
        }
    }

    /**
     * Saves current database(in XML format, using XStream) to file given to the
     * constructor.
     */
    private static void exportToXML() {
        try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile())) {
            XStream xstream = new XStream(new DomDriver());
            xstream
                    .alias("CreditCardDataBase", CreditCardDataBase.class
                    );
            xstream.alias(
                    "CreditCard", CreditCard.class
            );
            xstream.toXML(cardsDataBase, writer);
        } catch (FileNotFoundException ex) {
            //cannot create that file
            ex.printStackTrace();
        }

    }

    /**
     * Check if credit card with a given number exists in the database.
     *
     * @param number
     * @return
     */
    private static boolean cardExistsByNumber(String number) {
        return cardsDataBase.getCreditCards().stream()
                .anyMatch(card -> card.getCreditCardCodes()
                        .get(0).equals(
                                cryptCard(number,
                                        algorithm.getStandartOffset())));
    }

    /**
     * Get credit card with a given code.
     *
     * @param code
     * @return Card or or null if there is no such card.
     */
    private static CreditCard getCardByCode(String code) {
        return cardsDataBase.getCreditCards().stream()
                .filter(card -> card.getCreditCardCodes().contains(code))
                .findFirst().orElse(null);
    }

    /**
     * Get credit card with a given number.
     *
     * @param number
     * @return
     */
    private static CreditCard getCardByNumber(String number) {
        return cardsDataBase.getCreditCards().stream()
                .filter(card -> card.getCreditCardCodes()
                        .get(0).equals(cryptCard(number,
                                        algorithm.getStandartOffset())))
                .findFirst().orElse(null);
    }

    /**
     * Encrypt a given credit card number, offset is needed so the algorithm can
     * correctly encrypt.
     *
     * @param creditCardNumber
     * @param offset
     * @return credit card code.
     */
    private static String cryptCard(String creditCardNumber, int offset) {
        return algorithm.encrypt(creditCardNumber, offset);
    }

    /**
     * Decrypt a given credit card code, offset is needed so the algorithm can
     * correctly encrypt.
     *
     * @param creditCardCode
     * @return credit card number.
     */
    private static String decryptCard(String creditCardCode, int offset) {
        return algorithm.decrypt(creditCardCode, offset);
    }

}
