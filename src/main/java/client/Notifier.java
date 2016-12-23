package client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import Ice.Current;
import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;
import portal.Notification;
import portal.StreamInfo;
import portal._NotificationDisp;


public class Notifier extends Thread {

    private Map<String, StreamInfo> streams;
    private Ice.Communicator ic;

    public Notifier(Ice.Communicator ic) {
        this.streams = Collections.synchronizedMap(new HashMap<String, StreamInfo>());
        this.ic = ic;
    }

    public Collection<StreamInfo> getStreams() {
        return streams.values();
    }

    @Override
    public void run() {
        try {
            Ice.ObjectPrx obj = ic.stringToProxy("Notification/TopicManager:tcp -p 9999");
            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("NotificationAdapter", "tcp -p 10010");

            Notification notification = new NotificationI();
            Ice.ObjectPrx proxy = adapter.addWithUUID(notification).ice_oneway();
            adapter.activate();

            IceStorm.TopicPrx topic = null;
            topic = topicManager.retrieve("Stream");
            java.util.Map qos = null;
            topic.subscribeAndGetPublisher(qos, proxy);
        } catch (InvalidSubscriber invalidSubscriber) {
            invalidSubscriber.printStackTrace();
        } catch (AlreadySubscribed alreadySubscribed) {
            alreadySubscribed.printStackTrace();
        } catch (NoSuchTopic noSuchTopic) {
            noSuchTopic.printStackTrace();
        } catch (BadQoS badQoS) {
            badQoS.printStackTrace();
        }

    }

    private class NotificationI extends _NotificationDisp {

        public NotificationI() {
            super();
        }

        @Override
        public void streamAdded(StreamInfo streamInfo, Current __current) {
            // TODO(migafgarcia): check for stuff
            streams.put(streamInfo.id, streamInfo);
            printStreams();
        }

        @Override
        public void streamDeleted(String id, Current __current) {
            // TODO(migafgarcia): check for stuff
            streams.remove(id);
            printStreams();
        }

        private void printStreams() {
            System.out.println("Current streams:");
            streams.values().forEach(stream -> System.out.println(stream.name + " " + Arrays.toString(stream.keywords)));
            System.out.print("> ");
        }
    }

}
