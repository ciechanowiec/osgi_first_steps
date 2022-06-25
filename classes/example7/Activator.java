package tutorial.example7;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import tutorial.example6.service.SpellChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class implements a bundle that uses a spell checker
 * service to check the spelling of a passage. This bundle
 * is essentially identical to Example 5, in that it uses the
 * Service Tracker to monitor the dynamic availability of the
 * spell checker service. When starting this bundle, the thread
 * calling the start() method is used to read passages from
 * standard input. You can stop spell checking passages by
 * entering an empty line, but to start spell checking again
 * you must stop and then restart the bundle.
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    // Bundle's context
    private BundleContext context = null;
    // The service tracker object
    private ServiceTracker serviceTracker = null;

    /**
     * Creates a Service Tracker object to monitor spell checker
     * services. Enters a spell check loop where it reads passages
     * from standard input and checks their spelling using the
     * spell checker service
     * (NOTE: It is very bad practice to use the calling thread to perform
     * a lengthy process like this; this is only done for the purpose
     * of the tutorial)
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        // Create a service tracker to monitor spell check services
        String spellServiceName = SpellChecker.class.getName();
        Filter filter = this.context.createFilter(String.format("(objectClass=%s)", spellServiceName));
        serviceTracker = new ServiceTracker(this.context, filter, null);
        serviceTracker.open();

        try {
            System.out.println("Enter a blank line to exit");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String passage = "";

            // Loop endlessly
            while (true) {
                // Ask the user to enter a word
                System.out.println("Enter passage:");
                passage = in.readLine();

                // Get the selected spell checker service, if available
                SpellChecker spellChecker = (SpellChecker) serviceTracker.getService();

                // If the user entered a blank line, then exit the loop
                if (passage.isEmpty()) {
                    break;
                }
                // If there is no spell checker, then say so
                else if (spellChecker == null) {
                    System.out.println("No spell checker available");
                }
                // Otherwise check passage and print misspelled words
                else {
                    String[] errors = spellChecker.checkPassage(passage);
                    if (errors == null) {
                        System.out.println("The passage is correct");
                    } else {
                        System.out.println("Incorrect word(s):");
                        Stream.of(errors).forEach(word -> System.out.println("  " + word));
                    }
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
