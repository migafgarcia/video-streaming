package portal;

import IceStorm.NoSuchTopic;
import IceStorm.TopicExists;

/**
 *
 */
public class Server {

    public static void main(String[] args) {

        /*
            Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
            Obtain a proxy for the Stream topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
            Obtain a proxy for the Stream topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Monitor).
            Collect and report streams.
        */


        Ice.Communicator ic = null;

        try {

            // Initialize Ice run time
            ic = Ice.Util.initialize(args);

            // Creation of adapter
            Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("StreamerInterfaceAdapter", "default -p 10000");

            // Creation of servant
            Ice.Object streamerInterface = new StreamerInterfaceI();

            // Inform the adapter about the new servant
            adapter.add(streamerInterface, ic.stringToIdentity("StreamerInterface"));

            // Activate the adapter
            adapter.activate();

            Ice.ObjectPrx obj = ic.stringToProxy("StreamerInterface:default -p 10000");

            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            IceStorm.TopicPrx topic = null;

            topicManager.create("Stream");

            Ice.ObjectPrx pub = topic.getPublisher().ice_oneway();

            NotificationPrx notification = NotificationPrxHelper.uncheckedCast(pub);

            // Example
            String[] keywords = {"painting", "relaxing", "epic"};
            int i = 0;
            while(true) {
                Thread.sleep(1000);
                notification.addStream(new ShortStreamInfo("ASD" + i++, "Bob Ross", keywords));
            }



            // Blocks calling thread until shutdown has been called
            //ic.waitForShutdown();

        } catch (Ice.LocalException e) {
            e.printStackTrace();
        } catch (TopicExists topicExists) {
            topicExists.printStackTrace();
        } catch (InterruptedException e) {
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
