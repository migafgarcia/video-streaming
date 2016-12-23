package client;

import java.util.Scanner;

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

        int status = 0;
        Ice.Communicator ic = null;
        try {

            ic = Ice.Util.initialize(args);


            Ice.ObjectPrx base = ic.stringToProxy("ClientInterface:default -p 10000");

            ClientInterfacePrx clientInterface = ClientInterfacePrxHelper.checkedCast(base);


            if (clientInterface == null)
                throw new Error("Invalid proxy");

            StreamInfo[] streamInfos = clientInterface.getStreams();

            for (StreamInfo streamInfo : streamInfos) {
                System.out.println(streamInfo.id);
            }

            Notifier notifier = new Notifier(ic);
            notifier.start();

            Scanner scanner = new Scanner(System.in);

            while(scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
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
