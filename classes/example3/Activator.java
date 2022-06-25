package tutorial.example3;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import tutorial.example2.service.DictionaryService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    /**
     * Queries for all available dictionary services. If none
     * are found it simply prints a message and returns, otherwise
     * it reads words from standard input and checks for their
     * existence from the first dictionary that it finds
     */
    @Override
    public void start(BundleContext context) throws Exception {
        // Query fo all service references matching any language
        String dicServiceClassName = DictionaryService.class.getName();
        ServiceReference<?>[] references = context.getServiceReferences(
                                        dicServiceClassName, "(Language=*)");
        if (references != null) {
            try {
                System.out.println("Enter a blank line to exit");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String word = "";

                // Loop endlessly
                while (true) {
                    // Ask the user to enter a word
                    System.out.println("Enter word:");
                    word = in.readLine();

                    // If the user entered a blank line, then exit the loop
                    if (word.isEmpty()) {
                        break;
                    }

                    /* First, get a dictionary service and
                       then check if the word is correct */
                    DictionaryService dictionary = (DictionaryService) context.getService(references[0]);
                    if (dictionary.checkWord(word)) {
                        System.out.println("The word is correct");
                    } else {
                        System.out.println("The word is incorrect");
                    }

                    // Unget the dictionary service
                    context.ungetService(references[0]);
                }
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
        } else {
            System.out.println("Couldn't find any dictionary service...");
        }
    }

    /**
     * Does nothing since the framework will automatically unget any used services
     * @param context the framework context for the bundle
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        // NOTE: The service is automatically released
    }
}
