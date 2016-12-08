package portal;

/**
 *
 */
public class Portal {


    public static void main(String[] args) {

        /*
         *  Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
         *  Obtain a proxy for the Stream topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
         *  Obtain a proxy for the Stream topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Notification).
         *  Collect and report streams.
         */

        Ice.Communicator ic = null;

        try {

            // Initialize Ice run time
            ic = Ice.Util.initialize(args);

            // Creation of adapter
            Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("", "default -p 10000");

            Ice.ObjectPrx obj = ic.stringToProxy("Notification/TopicManager:tcp -p 9999");
            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            // 2 - Obtain a proxy for the Weather topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
            IceStorm.TopicPrx topic = null;
            while (topic == null) {
                try {
                    topic = topicManager.retrieve("Stream");
                } catch (IceStorm.NoSuchTopic ex) {
                    try {
                        topic = topicManager.create("Stream");
                    } catch (IceStorm.TopicExists e) {
                        // Another client created the topic.
                    }
                }
            }
            // 3 - Obtain a proxy for the Weather topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Monitor).
            Ice.ObjectPrx pub = topic.getPublisher().ice_oneway();
            NotificationPrx notificationPrx = NotificationPrxHelper.uncheckedCast(pub);

            // Creation of servant
            StreamerInterfaceI streamerInterface = new StreamerInterfaceI(notificationPrx);
            ClientInterfaceI clientInterface = new ClientInterfaceI(streamerInterface);

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
