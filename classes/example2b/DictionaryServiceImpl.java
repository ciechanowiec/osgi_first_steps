package tutorial.example2b;

import tutorial.example2.service.DictionaryService;

import java.util.Arrays;

class DictionaryServiceImpl implements DictionaryService {

    private final String[] knownWords = {"bienvenue", "dans", "l'univers"};

    /**
     * Determines if the passed in word is contained in the dictionary.
     * @param checkedWord the word to be checked
     * @return true if the word is in the dictionary,
     *         false otherwise
     */
    @Override
    public boolean checkWord(String checkedWord) {
        return Arrays.stream(knownWords)
                     .anyMatch(knownWord -> knownWord.equalsIgnoreCase(checkedWord));
    }
}
