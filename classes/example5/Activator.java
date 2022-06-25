package tutorial.example5;

import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import tutorial.example2.service.DictionaryService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a bundle that uses a dictionary
 * service to check for the proper spelling of a word by
 * checking for its existence in the dictionary. This bundle
 * is more complex than the bundle in Example 3 because it
 * monitors the dynamic availability of the dictionary
 * services. In other words, if the service it is using
 * departs, then it stops using it gracefully, or if it needs
 * a service and one arrives, then it starts using it
 * automatically. As before, the bundle uses the first service
 * that it finds and uses the calling thread of the
 * start() method to read words from standard input.
 * You can stop checking words by entering an empty line, but
 * to start checking words again you must stop and then restart
 * the bundle
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    // Bundle's context
    private BundleContext context = null;
    // The service tracker object
    private ServiceTracker serviceTracker = null;

    /**
     * Creates a service tracker to monitor dictionary services and
     * starts its "word checking loop". It will not be able to check
     * any words until the service tracker find a dictionary service;
     * any discovered dictionary service will be automatically used by
     * the client. It reads words from standard input and checks for
     * their existence in the discovered dictionary
     * (NOTE: It is very bad practice to use the calling thread to perform
     * a lengthy process like this; this is only done for the purpose
     * of the tutorial)
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        // Create a service tracker to monitor dictionary services
        String dicServiceName = DictionaryService.class.getName();
        Filter filter = this.context.createFilter(String.format("(&(objectClass=%s)(Language=*))", dicServiceName));
        serviceTracker = new ServiceTracker(this.context, filter, null);
        serviceTracker.open();

        try {
            System.out.println("Enter a blank line to exit");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String word = "";

            // Loop endlessly
            while (true) {
                // Ask the user to enter a word
                System.out.println("Enter word:");
                word = in.readLine();

                // Get the selected dictionary service, if available
                DictionaryService dictionary = (DictionaryService) serviceTracker.getService();

                // If the user entered a blank line, then exit the loop
                if (word.isEmpty()) {
                    break;
                }
                // If there is no dictionary, then say so
                else if (dictionary == null) {
                    System.out.println("No dictionary available");
                }
                // Otherwise print whether the word is correct or not
                else if (dictionary.checkWord(word)) {
                    System.out.println("The word is correct");
                } else {
                    System.out.println("The word is incorrect");
                }
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
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
