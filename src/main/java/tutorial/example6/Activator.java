package tutorial.example6;

import org.osgi.framework.*;
import tutorial.example2.service.DictionaryService;
import tutorial.example6.service.SpellChecker;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Activator implements BundleActivator, ServiceListener {

    // Bundle's context
    private BundleContext context = null;
    // List of available dictionary service references
    private final List<ServiceReference<?>> referencesList = new ArrayList<>();
    // Maps service references to service objects
    private final Map<ServiceReference<?>, Object> refToObjMap = new HashMap<>();
    // The spell checker service registration
    private ServiceRegistration<?> serviceRegistration = null;

    /**
     * Adds itself as a service listener and queries for all currently
     * available dictionary services. Any available dictionary services
     * are added to the service reference list. If dictionary services
     * are found, then the spell checker service is registered.
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        synchronized (referencesList) {
            // Listen for events pertaining to dictionary services
            String dicServiceClassName = DictionaryService.class.getName();
            String dicServicePropertiesFilter = String.format("(&(objectClass=%s)(Language=*))", dicServiceClassName);
            this.context.addServiceListener(this, dicServicePropertiesFilter);

            // Query for all dictionary services
            ServiceReference<?>[] retrievedReferences = this.context.getServiceReferences(dicServiceClassName,
                                                                                  "(Language=*)");

            // Add any dictionaries to the service reference list
            if (retrievedReferences != null) {
                Arrays.stream(retrievedReferences).collect(Collectors.toList()).forEach(traversedReference -> {
                    // Get the service object
                    Object service = this.context.getService(traversedReference);

                    // Make that the service is not being duplicated
                    if (service != null
                        && refToObjMap.get(traversedReference) == null) {
                        // Add to the reference list
                        referencesList.add(traversedReference);
                        // Map reference to service object for easy look up
                        refToObjMap.put(traversedReference, service);
                    }
                });

                /* Register spell checker service if there are any
                   dictionary services */
                if (!referencesList.isEmpty()) {
                    serviceRegistration = this.context.registerService(
                                          SpellChecker.class.getName(),
                                          new SpellCheckerImpl(), null);
                }
            }
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
     * Monitors the arrival and departure of dictionary services,
     * adding and removing them from the service reference list,
     * respectively. In the case where no more dictionary services
     * are available, the spell checker service is unregistered.
     * As soon as any dictionary service becomes available, the spell
     * checker service is registered
     * @param event the fired service event
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        synchronized (referencesList) {
            // Add the new dictionary service to the service lsit
            if (event.getType() == ServiceEvent.REGISTERED) {
                // Get the service object
                Object service = context.getService(event.getServiceReference());

                // Make that the service is not being duplicated
                if (service != null
                    && refToObjMap.get(event.getServiceReference()) == null) {
                    // Add to the reference list
                    referencesList.add(event.getServiceReference());
                    // Map reference to service object for easy look up
                    refToObjMap.put(event.getServiceReference(), service);

                    // Register spell checker service if necessary
                    if (serviceRegistration == null) {
                        serviceRegistration = context.registerService(
                                              SpellChecker.class.getName(),
                                              new SpellCheckerImpl(), null);
                    }
                }
            }
            // Remove departing service from the service list
            else if (event.getType() == ServiceEvent.UNREGISTERING) {
                // Make sure the service is in the list
                if (refToObjMap.get(event.getServiceReference()) != null) {
                    // Unget the service object
                    context.ungetService(event.getServiceReference());
                    // Remove service reverence
                    referencesList.remove(event.getServiceReference());
                    // Remove service reference from map
                    refToObjMap.remove(event.getServiceReference());

                    /* If there are no more dictionary services,
                       then unregister the spell checker service */
                    if (referencesList.isEmpty()) {
                        serviceRegistration.unregister();
                        serviceRegistration = null;
                    }
                }
            }
        }
    }

    private class SpellCheckerImpl implements SpellChecker {

        /**
         * Checks the given passage for misspelled words.
         * @param passage the passage to spell check
         * @return An array of misspelled words or null
         *         if no words are misspelled
         */
        @Override
        public String[] check(String passage) {
            // No misspelled words for an empty string
            if (passage == null || passage.isEmpty()) {
                return null;
            }

            Collection<String> errorList = new ArrayList<>();

            // Tokenize the passage using spaces and punctuation
            StringTokenizer tokenizer = new StringTokenizer(passage, " ,.!?;:");

            // Lock the service list
            synchronized (referencesList) {
                // Loop through each word in the passage
                while (tokenizer.hasMoreTokens()) {
                    String word = tokenizer.nextToken();
                    boolean correct = false;
                    // Check each available dictionary for the current word
                    for (int i = 0; (!correct) && (i < referencesList.size()); i++) {
                        DictionaryService dictionaryService =
                                (DictionaryService) refToObjMap.get(referencesList.get(i));
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
            }

            // Return null if no words are incorrect
            if (errorList.isEmpty()) {
                return null;
            }

            // Return the array of incorrect words
            return (String[]) errorList.toArray(new String[errorList.size()]);
        }
    }
}
