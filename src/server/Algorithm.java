package server;

import java.util.ArrayList;
import java.util.List;

/**
 * Different Algorithms
 *
 * @author petar
 */
public class Algorithm {

    final int SPECIAL_KEY;

    /**
     *
     * @param specialKey
     */
    public Algorithm(int specialKey) {
        SPECIAL_KEY = specialKey;
    }

    /**
     * Simple encryption is a cipher algorithm shifts the symbol with
     * SPECIAL_KEY for example: if SPECIAL_KEY = 5 then: 1->6, 2->7, 3->8, 4->9,
     * 5->0, 6->1 ...
     */
    private class SimpleEncryption implements CryptAlgorithm {

        @Override
        public int getStandartOffset() {
            return SPECIAL_KEY;
        }

        @Override
        public String encrypt(String creditCardNumber, int offset) {
            String[] cardNumber = creditCardNumber.split("");
            StringBuilder creditCardCode = new StringBuilder();
            for (String digit : cardNumber) {
                if (digit.compareTo("0") >= 0 && digit.compareTo("9") <= 0) {
                    int x = (Integer.parseInt(digit)
                            + offset) % 10;
                    creditCardCode.append(x);
                } else {
                    creditCardCode.append(digit);
                }
            }
            return creditCardCode.toString();
        }

        @Override
        public String decrypt(String creditCardCode, int offset) {
            String[] cardCode = creditCardCode.split("");
            StringBuilder creditCardNumber = new StringBuilder();
            for (String digit : cardCode) {
                if (digit.compareTo("0") >= 0 && digit.compareTo("9") <= 0) {
                    int x = (Integer.parseInt(digit)
                            + 10 + offset) % 10;
                    creditCardNumber.append(x);
                } else {
                    creditCardNumber.append(digit);
                }
            }
            return creditCardNumber.toString();
        }
    }

    /**
     * a reference to the SimpleEncryption algorithm
     *
     * @return
     */
    public CryptAlgorithm substitutionCipher() {
        return new SimpleEncryption();
    }

    /**
     * EXAMPLE:WE ARE DISCOVERED. FLEE AT ONCE
     *
     * W . . . E . . . C . . . R . . . L . . . T . . . E
     * . E . R . D . S . O . E . E . F . E . A . O . C .
     * . . A . . . I . . . V . . . D . . . E . . . N . .
     * ref:http://en.wikipedia.org/wiki/Rail_fence_cipher
     */
    class RailFence implements CryptAlgorithm {

        @Override
        public String encrypt(String plainText, int offset) {
            if (offset == 0 || offset == 1) {
                return plainText;
            }
            ArrayList<String> railFence = new ArrayList<>();
            for (int i = 0; i < offset; i++) {
                railFence.add("");
            }

            int number = 0;
            int increment = 1;
            for (char c : plainText.toCharArray()) {
                if (number + increment == offset) {
                    increment = -1;
                } else if (number + increment == -1) {
                    increment = 1;
                }
                railFence.set(number, railFence.get(number) + c);
//            railFence[number] += c;
                number += increment;
            }

            String buffer = "";
            for (String s : railFence) {
                buffer += s;
            }
            return buffer;
        }

        @Override
        public String decrypt(String cipherText, int offset) {
            if (offset == 0 || offset == 1) {
                return cipherText;
            }
            int cipherLength = cipherText.length();
            List<List<Integer>> railFence = new ArrayList<>();
            for (int i = 0; i < offset; i++) {
                railFence.add(new ArrayList<>());
            }

            int number = 0;
            int increment = 1;
            for (int i = 0; i < cipherLength; i++) {
                if (number + increment == offset) {
                    increment = -1;
                } else if (number + increment == -1) {
                    increment = 1;
                }
//              railFence is a matrix of integers - indicating the 
//              correct indexes order
                railFence.get(number).add(i);
                number += increment;
            }

            int counter = 0;
            char[] buffer = new char[cipherLength];
            for (int i = 0; i < offset; i++) {
                for (int j = 0; j < railFence.get(i).size(); j++) {
                    //
                    buffer[railFence.get(i).get(j)] = cipherText.toCharArray()[counter];
//            buffer[railFence[i][j]] = cipherText[counter];
                    counter++;
                }
            }

            return new String(buffer);
        }

        @Override
        public int getStandartOffset() {
            return SPECIAL_KEY;
        }
    }

    public CryptAlgorithm railFenceCipher() {
        return new RailFence();
    }

}
