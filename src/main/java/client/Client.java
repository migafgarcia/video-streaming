package client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;
import portal.ClientInterfacePrx;
import portal.ClientInterfacePrxHelper;
import portal.Notification;
import portal.StreamInfo;

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

        Ice.Communicator ic = null;
        try {

            ic = Ice.Util.initialize(args);


            Ice.ObjectPrx base = ic.stringToProxy("ClientInterface:default -p 10000");

            ClientInterfacePrx clientInterface = ClientInterfacePrxHelper.checkedCast(base);


            if (clientInterface == null)
                throw new Error("Invalid proxy");

            Ice.ObjectPrx obj = ic.stringToProxy("Notification/TopicManager:tcp -p 9999");
            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("NotificationAdapter", "tcp -p 10010");

            NotificationI notification = new NotificationI(clientInterface.getStreams());
            Ice.ObjectPrx proxy = adapter.addWithUUID(notification).ice_oneway();
            adapter.activate();

            IceStorm.TopicPrx topic = null;

            topic = topicManager.retrieve("Stream");
            java.util.Map qos = null;
            topic.subscribeAndGetPublisher(qos, proxy);

            Pattern listPattern = Pattern.compile("\\s*l\\s*");
            Pattern connectPattern = Pattern.compile("\\s*c\\s*(\\w+)\\s*");

            Scanner scanner = new Scanner(System.in);
            String line;


            while(scanner.hasNextLine()) {
                line = scanner.nextLine();

                if(listPattern.matcher(line).matches()) {
                    System.out.println("LISTING");
                    notification.printStreams();

                }
                else  {
                    Matcher matcher = connectPattern.matcher(line);

                    if(matcher.matches()) {
                        System.out.println("CONNECTING TO " + matcher.group(1));
                        StreamInfo stream = notification.getStreamInfo(matcher.group(1));
                        if(stream == null)
                            System.out.println("Stream doesn't exist");
                        else {
                            Process p = new ProcessBuilder("vlc", "tcp://" + stream.ip + ":" + stream.port).start();
                            p.waitFor();
                        }
                    }
                }

            }

            ic.waitForShutdown();

            topic.unsubscribe(proxy);
        } catch (Ice.LocalException e) {
            e.printStackTrace();
        } catch (InvalidSubscriber invalidSubscriber) {
            invalidSubscriber.printStackTrace();
        } catch (AlreadySubscribed alreadySubscribed) {
            alreadySubscribed.printStackTrace();
        } catch (NoSuchTopic noSuchTopic) {
            noSuchTopic.printStackTrace();
        } catch (BadQoS badQoS) {
            badQoS.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ic != null) {
            // Clean up
            //
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
