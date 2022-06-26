package tutorial.example2b;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import tutorial.example2.service.DictionaryService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This class implements a simple bundle that uses the bundle
 * context to register a French language dictionary service
 * with the OSGi framework. The dictionary service interface is
 * defined in a separate class file and is implemented by an
 * inner class.
 */
public class Activator implements BundleActivator {

    /**
     * Registers an instance of a dictionary service using the
     * bundle context; attaches properties to the service that
     * can be queried when performing a service look-up
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> properties = new Hashtable<>() {{
           put("Language", "French");
        }};
        String serviceName = DictionaryService.class.getName();
        context.registerService(serviceName, new DictionaryServiceImpl(), properties);
    }

    /**
     * Does nothing since the framework will automatically unregister
     * any registered services
     * @param context the framework context for the bundle
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        // The service is unregistered automatically
    }
}
