package client;

import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;

public class Client {
    public static void main(String[] args) {
        /*
         *  Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
         *  Create an object adapter to host our Notification servant.
         *  Instantiate the Notification servant and activate it with the object adapter.
         *  Subscribe to the Stream topic.
         *  Process add and delete stream messages until shutdown.
         *  Unsubscribe from the Stream topic.
         */

        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);

            Ice.ObjectPrx obj = ic.stringToProxy("Portal:default -p 10000");

            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            Ice.ObjectAdapter adapter = ic.createObjectAdapter("NotificationAdapter");


            adapter.activate();

            IceStorm.TopicPrx topic = null;

            try {
                topic = topicManager.retrieve("Stream");
                java.util.Map qos = null;
            }
            catch (IceStorm.NoSuchTopic ex) {
                // Error! No topic found!
            }

            ic.waitForShutdown();


        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        }
        if (ic != null) {
            // Clean up
            //
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);

    }
}
