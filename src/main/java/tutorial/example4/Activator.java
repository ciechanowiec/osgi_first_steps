package tutorial.example4;

import org.osgi.framework.*;
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
public class Activator implements BundleActivator, ServiceListener {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    // Bundle's context
    private BundleContext context = null;
    // The service reference being used
    private ServiceReference<?> serviceReference = null;
    // The service object being used
    private DictionaryService dictionary = null;

    /**
     * Adds itself as a listener for service events, then queries
     * for available dictionary services. If any dictionaries are
     * found it gets a reference to the first one available and
     * then stats its "word checking loop". If no dictionaries are
     * found, then it just goes directly into its "word checking loop",
     * but it will not be able to check any words until a dictionary
     * arrives; any arriving dictionary service will be automatically
     * used by the client if a dictionary is not already in use. Once
     * it has dictionary, it reads words from standard input and checks
     * for the existence in the dictionary that it is using.
     * (NOTE: It is very bad practice to use the calling thread to perform
     * a lengthy process like this; this is only done for the purpose
     * of the tutorial)
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        /* We synchronize while registering the service listener and
           performing our initial dictionary service lookup since we
           don't want to receive service events when looking up the
           dictionary service, if one exists
         */
        synchronized (this) {
            // Listen for events pertaining to dictionary services
            String dicServiceName = DictionaryService.class.getName();
            this.context.addServiceListener(this,
                         String.format("(&(objectClass=%s)(Language=*))", dicServiceName));

            // Query for any service references matching any language
            ServiceReference<?>[] references = this.context.getServiceReferences(dicServiceName, "(Language=*)");

            /* If we found any dictionary services, then just get
               a reference to the first one so we can use it */
            if (references != null) {
                serviceReference = references[0];
                dictionary = (DictionaryService) this.context.getService(serviceReference);
            }
        }

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

    /**
     * Checks to see if the service we are using is leaving or
     * tries to get a service if we need one
     * @param event the fired service event
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");

        /* If a dictionary service was registered, see if we need one.
           If so, get a reference to it */
        if (event.getType() == ServiceEvent.REGISTERED) {
            if (serviceReference == null) {
                // Get a reference to the service object
                serviceReference = event.getServiceReference();
                dictionary = (DictionaryService) context.getService(serviceReference);
            }
        }
        /* If a dictionary service was unregistered, see if it
           was the one we were using. If so, unget the service
           and try to query to get another one */
        else if (event.getType() == ServiceEvent.UNREGISTERING) {
            if (event.getServiceReference() == serviceReference) {
                // Unget service object and null references
                context.ungetService(serviceReference);
                serviceReference = null;
                dictionary = null;

                // Query to see if we can get another service
                ServiceReference<?>[] references = null;
                try {
                    String dicServiceName = DictionaryService.class.getName();
                    // Query for any service references matching any language
                    references = this.context.getServiceReferences(dicServiceName, "(Language=*)");
                } catch (InvalidSyntaxException exception) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                if (references != null) {
                    // Get a reference to the first service object
                    serviceReference = references[0];
                    dictionary = (DictionaryService) context.getService(serviceReference);
                }
            }
        }
    }
}
