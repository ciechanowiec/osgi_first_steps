package tutorial.example2.service;

/**
 * A simple service interface that defines a dictionary service.
 * A dictionary service simply verifies the existence if a word.
 */
public interface DictionaryService {

    /**
     * Check for the existence of a word.
     * @param checkedWord the word to be checked
     * @return true if the word is in the dictionary,
     *         false otherwise
     */
    boolean checkWord(String checkedWord);
}
