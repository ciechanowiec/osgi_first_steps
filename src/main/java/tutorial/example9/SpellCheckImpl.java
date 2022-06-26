package tutorial.example9;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import tutorial.example2.service.DictionaryService;
import tutorial.example6.service.SpellChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class re-implements the spell check service of Example 6. This service
 * implementation behaves exactly like the one in Example 6, specifically, it
 * aggregates all available dictionary services, monitors their dynamic
 * availability, and only offers the spell check service if there are dictionary
 * services available. The service implementation is greatly simplified, though,
 * by using the Service Component Runtime. Notice that there is no OSGi reference
 * application code; instead, the annotations describe the service dependencies
 * to the Service Component Runtime, which automatically manages them and also
 * automatically registers the spell check services as appropriate
 */
public class SpellCheckImpl implements SpellChecker {

    /**
     * List of service objects
     * This field is managed by the Service Component Runtime and updated
     * with the current set of available dictionary services.
     * At least one dictionary service is required
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.AT_LEAST_ONE)
    private volatile List<DictionaryService> servicesObjectsList;

    /**
     * Checks a given passage for spelling errors. A passage is any
     * number of words separated by a space and any of the following
     * punctuation marks: comma (,), period (.), exclamation mark (!),
     * question mark (?), semi-colon (;), and colon(:)
     * @param passage the passage to spell check
     * @return An array of misspelled words or null if no
     *         words are misspelled
     */
    @Override
    public String[] checkPassage(String passage) {
        // No misspelled words for an empty string
        if (passage == null || passage.isEmpty()) {
            return null;
        }

        Collection<String> errorList = new ArrayList<>();

        // Tokenize the passage using spaces and punctuation
        StringTokenizer tokenizer = new StringTokenizer(passage, " ,.!?;:");

        /* Put the current set of services in a local field.
           The field servicesObjectsList will be modified concurrently */
        final List<DictionaryService> localServices = servicesObjectsList;

        // Loop through each word in the passage
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            boolean correct = false;

            // Check each available dictionary for the current word
            for (final DictionaryService dictionaryService : localServices) {
                if (dictionaryService.checkWord(word)) {
                    correct = true;
                }
            }

            /* If the word is not correct, the add it
               to the incorrect word list */
            if (!correct) {
                errorList.add(word);
            }
        }

        // Return null if no words are incorrect
        if (errorList.isEmpty()) {
            return null;
        }

        // Return the array of incorrect words
        return errorList.toArray(new String[errorList.size()]);
    }
}
