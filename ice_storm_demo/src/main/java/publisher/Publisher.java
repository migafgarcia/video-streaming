package publisher;

import java.util.Random;
import java.util.UUID;

import shared.Measurement;
import shared.MonitorPrx;
import shared.MonitorPrxHelper;

import static Ice.Application.communicator;

/**
 *
 */
public class Publisher {
    /**
     * The implementation of our weather measurement collector application can be summarized easily:
     * 1 - Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
     * 2 - Obtain a proxy for the Weather topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
     * 3 - Obtain a proxy for the Weather topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Monitor).
     * 4 - Collect and report measurements.
     *
     * @param args
     */
    public static void main(String[] args) {
        // 1 - Obtain a proxy for the TopicManager. This is the primary IceStorm object, used by both publishers and subscribers.
        Ice.ObjectPrx obj = communicator().stringToProxy("IceStorm/TopicManager:tcp -p 9999");
        IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

        // 2 - Obtain a proxy for the Weather topic, either by creating the topic if it does not exist, or retrieving the proxy for the existing topic.
        IceStorm.TopicPrx topic = null;
        while (topic == null) {
            try {
                topic = topicManager.retrieve("Weather");
            } catch (IceStorm.NoSuchTopic ex) {
                try {
                    topic = topicManager.create("Weather");
                } catch (IceStorm.TopicExists e) {
                    // Another client created the topic.
                }
            }
        }
        // 3 - Obtain a proxy for the Weather topic's "publisher object." This proxy is provided for the purpose of publishing messages, and therefore is narrowed to the topic interface (Monitor).
        Ice.ObjectPrx pub = topic.getPublisher().ice_oneway();
        MonitorPrx monitor = MonitorPrxHelper.uncheckedCast(pub);

        // 4 - Collect and report measurements.
        while (true) {
            Measurement m = getMeasurement();
            monitor.report(m);
        }


    }

    private static Measurement getMeasurement() {
        String tower = UUID.randomUUID().toString();

        Random rand = new Random();

        float windSpeed = rand.nextFloat();

        short windDirection = (short) rand.nextInt();

        float temperature = rand.nextFloat();

        return new Measurement(tower, windSpeed, windDirection, temperature);
    }


}
