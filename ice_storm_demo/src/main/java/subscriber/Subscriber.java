package subscriber;

import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import shared.Monitor;


public class Subscriber {
    /**
     * Our weather measurement subscriber implementation takes the following steps:
     * 1 - Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
     * 2 - Create an object adapter to host our Monitor servant.
     * 3 - Instantiate the Monitor servant and activate it with the object adapter.
     * 4 - Suscribe to the Weather topic.
     * 5 - Process report messages until shutdown.
     * 6 - Unsubscribe from the Weather topic.
     *
     * @param args
     */
    public static void main(String[] args) {
        Ice.Communicator communicator = Ice.Util.initialize(args);
        Ice.ObjectPrx obj = communicator.stringToProxy("Notification/TopicManager:tcp -p 9999");
        IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

        Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MonitorAdapter", "tcp -p 10000");

        Monitor monitor = new MonitorI();
        Ice.ObjectPrx proxy = adapter.addWithUUID(monitor).ice_oneway();
        adapter.activate();

        IceStorm.TopicPrx topic = null;
        try {
            topic = topicManager.retrieve("Weather");
            java.util.Map qos = null;
            topic.subscribeAndGetPublisher(qos, proxy);
        }
        catch (IceStorm.NoSuchTopic ex) {
            ex.printStackTrace();
        } catch (AlreadySubscribed alreadySubscribed) {
            alreadySubscribed.printStackTrace();
        } catch (InvalidSubscriber invalidSubscriber) {
            invalidSubscriber.printStackTrace();
        } catch (BadQoS badQoS) {
            badQoS.printStackTrace();
        }

        communicator.waitForShutdown();

        topic.unsubscribe(proxy);

    }
}
