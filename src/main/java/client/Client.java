package client;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import IceStorm.AlreadySubscribed;
import IceStorm.BadQoS;
import IceStorm.InvalidSubscriber;
import IceStorm.NoSuchTopic;
import portal.ClientInterfacePrx;
import portal.ClientInterfacePrxHelper;
import portal.StreamInfo;

public class Client {

    private static final String HELP = "Video Streaming Client\n\tl\t\t\t\t\tlist available streams\n\tc [name]\t\t\tconnect to stream with the given name. e.g.: c nicestream, c \"Very good stream\"\n\ts [keyword]\t\t\tsearch for streams with the given keyword. e.g.: s cars, c football\n";
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

            Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("NotificationAdapter", "tcp");

            NotificationI notification = new NotificationI(clientInterface.getStreams());
            Ice.ObjectPrx proxy = adapter.addWithUUID(notification).ice_oneway();
            adapter.activate();

            IceStorm.TopicPrx topic = null;

            topic = topicManager.retrieve("Stream");
            java.util.Map qos = null;
            topic.subscribeAndGetPublisher(qos, proxy);

            final IceStorm.TopicPrx finalTopic = topic;
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> finalTopic.unsubscribe(proxy)));

            Pattern listPattern = Pattern.compile("\\s*l\\s*");
            Pattern connectPattern1 = Pattern.compile("\\s*c\\s+(\\w+)\\s*");
            Pattern connectPattern2 = Pattern.compile("\\s*c\\s+\"([\\w\\s]+)\"\\s*");
            Pattern searchPattern = Pattern.compile("\\s*s\\s+(\\w+)\\s*");


            Scanner scanner = new Scanner(System.in);
            String line;


            System.out.println(HELP);
            System.out.print("> ");
            while(scanner.hasNextLine()) {

                line = scanner.nextLine();

                Matcher connectMatcher1 = connectPattern1.matcher(line);
                Matcher connectMatcher2 = connectPattern2.matcher(line);
                Matcher searchMatcher = searchPattern.matcher(line);


                if(listPattern.matcher(line).matches()) {
                    System.out.println("LISTING");
                    notification.printStreams();

                }
                else if(connectMatcher1.matches() || connectMatcher2.matches()) {

                    String streamName = null;

                    if(connectMatcher1.matches())
                        streamName = connectMatcher1.group(1);
                    else
                        streamName = connectMatcher2.group(1);



                    System.out.println("CONNECTING TO " + streamName);
                    StreamInfo stream = notification.getByName(streamName);
                    if(stream == null)
                        System.out.println("Stream doesn't exist");
                    else {
                        Process p = new ProcessBuilder("vlc", "tcp://" + stream.ip + ":" + stream.port).start();
                    }
                }
                else if(searchMatcher.matches()) {
                    System.out.println("SEARCH: " + searchMatcher.group(1));
                    notification.search(searchMatcher.group(1));
                }
                else {
                    System.out.println(HELP);
                }

                System.out.print("> ");
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
