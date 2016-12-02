package streamer;

import portal.StreamInfo;
import portal.StreamerInterfacePrx;
import portal.StreamerInterfacePrxHelper;

/**
 *
 */
public class Streamer {

    public static void main(String[] args) {

        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);

            Ice.ObjectPrx base = ic.stringToProxy("StreamerInterface:default -p 10000");

            StreamerInterfacePrx streamerInterface = StreamerInterfacePrxHelper.checkedCast(base);

            if (streamerInterface == null)
                throw new Error("Invalid proxy");

            String[] keywords = {"games", "stuff", "cats"};

            String id = streamerInterface.addStream(new StreamInfo("Some dude plays some game", "TCP", "127.0.0.1", 6666, 1920, 1080, 400, keywords));

            if(!id.equals("")) {
                new ProcessBuilder().start();
            }


        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
