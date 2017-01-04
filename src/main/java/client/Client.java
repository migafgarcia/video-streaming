package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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

    private static final String HELP = "Video Streaming Client\n\tl\t\t\t\t\tlist available streams\n\tc [number]\t\t\tconnect to stream with the given number on the previously listed list. e.g.: c 3\n\ts [keyword]\t\t\tsearch for streams with the given keyword. e.g.: s cars, c football\n\te\t\t\t\t\texit";

    public static void main(String[] args) {

        // Read arguments
        if (args.length < 1) {
            System.out.println("Wrong number of arguments:\n\t Provide arguments [PORTAL_URL]");
            System.exit(0);
        }

        String portalUrl = args[0];


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


            Ice.ObjectPrx base = ic.stringToProxy("ClientInterface:tcp -h " + portalUrl + " -p 10000");

            ClientInterfacePrx clientInterface = ClientInterfacePrxHelper.checkedCast(base);


            if (clientInterface == null)
                throw new Error("Invalid proxy");

            Ice.ObjectPrx obj = ic.stringToProxy("Notification/TopicManager:tcp -h " + portalUrl + " -p 9999");
            IceStorm.TopicManagerPrx topicManager = IceStorm.TopicManagerPrxHelper.checkedCast(obj);

            Ice.ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("NotificationAdapter", "tcp");

            NotificationI notification = new NotificationI(clientInterface.getStreams());
            Ice.ObjectPrx proxy = adapter.addWithUUID(notification).ice_oneway();
            adapter.activate();

            IceStorm.TopicPrx topic;

            topic = topicManager.retrieve("Stream");
            topic.subscribeAndGetPublisher(null, proxy);

            final IceStorm.TopicPrx finalTopic = topic;
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> finalTopic.unsubscribe(proxy)));

            Pattern listPattern = Pattern.compile("\\s*l\\s*");
            Pattern exitPattern = Pattern.compile("\\s*e\\s*");
            Pattern connectPattern = Pattern.compile("\\s*c\\s+(\\d+)\\s*");
            Pattern searchPattern = Pattern.compile("\\s*s\\s+(\\w+)\\s*");


            Scanner scanner = new Scanner(System.in);
            String line;

            ArrayList<StreamInfo> currentList = new ArrayList<>();

            Collection<StreamInfo> streams = notification.getStreams();
            if(streams.size() == 0)
                System.out.println("No streams available");
            else
                streams.forEach(stream -> {
                    System.out.println(currentList.size()+1 + " " + stream.name + " " + Arrays.toString(stream.keywords));
                    currentList.add(stream);
                });


            System.out.println(HELP);
            System.out.print("> ");
            while(scanner.hasNextLine()) {

                line = scanner.nextLine();

                Matcher connectMatcher = connectPattern.matcher(line);
                Matcher searchMatcher = searchPattern.matcher(line);


                if(exitPattern.matcher(line).matches()) {
                    System.out.println("EXIT");
                    System.exit(0);
                }
                else if(listPattern.matcher(line).matches()) {
                    System.out.println("LISTING");
                    currentList.clear();
                    streams = notification.getStreams();
                    if(streams.size() == 0)
                        System.out.println("No streams available");
                    else
                        streams.forEach(stream -> {
                            System.out.println(currentList.size()+1 + " " + stream.name + " " + Arrays.toString(stream.keywords));
                            currentList.add(stream);
                        });
                }
                else if(connectMatcher.matches()) {

                    int i = new Integer(connectMatcher.group(1)) - 1;

                    if(i < currentList.size() && i >= 0) {

                        StreamInfo stream = currentList.get(i);
                        System.out.println("CONNECTING TO " + stream.name);
                        if(stream == null)
                            System.out.println("Stream doesn't exist");
                        else {
                            new ProcessBuilder("vlc", "tcp://" + stream.hostname + ":" + stream.port).start();
                        }

                    }
                    else {
                        System.out.println("Can't connect to stream");
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
        } catch (Ice.LocalException | IOException | BadQoS | NoSuchTopic | AlreadySubscribed | InvalidSubscriber e) {
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
