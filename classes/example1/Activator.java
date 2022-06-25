package tutorial.example1;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * This class implements a simple bundle that utilizes the OSGi
 * framework's events mechanism to listen for service events. Upon
 * Receiving a service event, it prints out the event's details.
 */
public class Activator implements BundleActivator, ServiceListener {

    /**
     * Prints a message and adds itself to the bundle
     * context as a service listener
     * @param context the framework context for the bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting to listen for service events.");
        context.addServiceListener(this);
    }

    /**
     * Prints a message and removes itself from the bundle
     * context as a service listener.
     * @param context the framework context for the bundle
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        context.removeServiceListener(this);
        System.out.println("Stopped listening for service events.");
        /* It is not required that we remove the listener here, since the
           framework will do it automatically anyway */
    }

    /**
     * Prints the details of any service event from the framework
     * @param event the fired service event
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");
        if (event.getType() == ServiceEvent.REGISTERED) {
            System.out.println("Ex1: Service of type " + objectClass[0] + " registered.");
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            System.out.println("Ex1: Service of type " + objectClass[0] + " unregistered.");
        } else if (event.getType() == ServiceEvent.MODIFIED) {
            System.out.println("Ex1: Service of type " + objectClass[0] + " modified.");
        }
    }
}
