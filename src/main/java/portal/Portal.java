package portal;

import IceStorm.TopicExists;

/**
 *
 */
public class Portal {

    public static void main(String[] args) {

        /*
         *  Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
         *  Obtain a proxy for the Stream topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
         *  Obtain a proxy for the Stream topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Monitor).
         *  Collect and report streams.
         */


        Ice.Communicator ic = null;

        try {

            // Initialize Ice run time
            ic = Ice.Util.initialize(args);

            // Creation of adapter
            Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("", "default -p 10000");

            // Creation of servant
            Ice.Object streamerInterface = new StreamerInterfaceI();
            Ice.Object clientInterface = new ClientInterfaceI();

            // Inform the adapter about the new servant
            adapter.add(streamerInterface, ic.stringToIdentity("StreamerInterface"));
            adapter.add(clientInterface, ic.stringToIdentity("ClientInterface"));

            // Activate the adapter
            adapter.activate();



            // Blocks calling thread until shutdown has been called
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
