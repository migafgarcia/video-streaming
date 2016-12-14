package portal;

/**
 *
 */
public class Portal {


    public static void main(String[] args) {

        Ice.Communicator ic = null;

        try {
            /*
             * We initialize the Ice run time by calling Ice.Util.initialize.
             * (We pass args to this call because the server may have command-line arguments that are of interest to the run time;
             * for this example, the server does not require any command-line arguments.)
             * The call to initialize returns an Ice.Communicator reference, which is the main object in the Ice run time.
             */
            ic = Ice.Util.initialize(args);

            /*
             * We create an object adapter by calling createObjectAdapterWithEndpoints on the Communicator instance.
             * The arguments we pass are "PortalAdapter" (which is the name of the adapter) and "default -p 10000",
             * which instructs the adapter to listen for incoming requests using the default protocol (TCP/IP) at port number 10000.
             */
            Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("PortalAdapter", "default -p 10000");
            /*
             * Obtain a proxy for the TopicManager.
             * This is the primary IceStorm object, used by both publishers and subscribers.
             */
            Ice.ObjectPrx obj = ic.stringToProxy("Notification/TopicManager:tcp -p 9999");
            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            /*
             * Obtain a proxy for the Stream topic, either by creating the topic if it does not exist,
             * or retrieving the proxy for the existing topic.
             */
            IceStorm.TopicPrx topic = null;
            while (topic == null) {
                try {
                    topic = topicManager.retrieve("Stream");
                } catch (IceStorm.NoSuchTopic ex) {
                    try {
                        topic = topicManager.create("Stream");
                    } catch (IceStorm.TopicExists e) {
                        // Another client created the topic??
                    }
                }
            }

            /*
             * Obtain a proxy for the Stream topic's "publisher object."
             * This proxy is provided for the purpose of publishing messages,
             * and therefore is narrowed to the topic interface (Notification).
             */
            Ice.ObjectPrx pub = topic.getPublisher().ice_oneway();
            NotificationPrx notificationPrx = NotificationPrxHelper.uncheckedCast(pub);

            /*
             * Instantiation of the Manager.
             * This will instantiate both Streamer and Client interfaces and use the
             * NotificationPrx to notify clients of new and deleted streams.
             */
            Manager manager = new Manager(notificationPrx);

            /*
             * At this point, the server-side run time is initialized and we create a servant for our Streamer and Client
             * interfaces by obtaining both StreamerInterfaceI and ClientInterfaceI objects.
             * We inform the object adapter of the presence of a new servant by calling add on the adapter;
             * the arguments to add are the servant we have just instantiated, plus an identifier.
             * For example, the string "StreamerInterface" is the name of the StreamerInterface Ice object.
             */
            adapter.add(manager.getStreamerInterface(), ic.stringToIdentity("StreamerInterface"));
            adapter.add(manager.getClientInterface(), ic.stringToIdentity("ClientInterface"));

            /*
             * Next, we activate the adapter by calling its activate method.
             * (The adapter is initially created in a holding state;
             * this is useful if we have many servants that share the same adapter and do not want requests
             * to be processed until after all the servants have been instantiated.)
             */
            adapter.activate();

            /*
             * Finally, we call waitForShutdown.
             * This call suspends the calling thread until the server implementation terminates,
             * either by making a call to shut down the run time, or in response to a signal.
             * (For now, we will simply interrupt the server on the command line when we no longer need it.)
             */
            ic.waitForShutdown();

        } catch (Ice.LocalException e) {
            e.printStackTrace();
        }

        if (ic != null) {
            // Clean up
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        System.exit(0);

    }

}
