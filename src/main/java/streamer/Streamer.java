package streamer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;

import portal.StreamerInterfacePrx;
import portal.StreamerInterfacePrxHelper;
import utils.Md5;

/**
 *  Streamer client
 */
public class Streamer {

    public static void main(String[] args) {

        // TODO(migafgarcia): add shutdown hook for closing stream in the portal OR heartbeat in portal (or both)

        // Read arguments
        if(args.length < 5) {
            System.out.println("Wrong number of arguments:\n\t $ java streamer.Streamer [PORTAL_URL] [NAME] [VIDEO] [WIDTH] [HEIGHT] [KEYWORD]...");
            System.exit(0);
        }

        // The URL of the Portal
        String portalUrl = args[0];

        // The name that will appear on the Portal
        String name = args[1];

        // The video file
        String video = args[2];

        // The width in pixels
        int width = Integer.parseInt(args[3]);

        // The height in pixels
        int height = Integer.parseInt(args[4]);

        // Keywords defining the stream
        String[] keywords = Arrays.copyOfRange(args, 5, args.length);

        // Key generated that will allow a Streamer to perform actions on the Portal concerning his stream
        String key = Md5.md5(new ByteArrayInputStream(new String(name + width + height + Arrays.toString(keywords) + System.currentTimeMillis()).getBytes()));

        int status = 0;

        try {
            /*
             * We initialize the Ice run time by calling Ice.Util.initialize.
             * (We pass args to this call because the server may have command-line arguments that are of interest to the run time;
             * for this example, the server does not require any command-line arguments.)
             * The call to initialize returns an Ice.Communicator reference, which is the main object in the Ice run time.
             */
            Ice.Communicator ic = Ice.Util.initialize(args);

            /*
             * The next step is to obtain a proxy for the StreamerInterface object.
             * We create a proxy by calling stringToProxy on the communicator,
             * with the string "StreamerInterface:default -p 10000".
             * Note that the string contains the object identity and the port number that were used by the server.
             * (Obviously, hard-coding object identities and port numbers into our applications is a bad idea,
             * but it will do for now; we will see more architecturally sound ways of doing this when we discuss IceGrid.)
             */
            Ice.ObjectPrx base = ic.stringToProxy("StreamerInterface:tcp -h " + portalUrl + " -p 10000");

            /*
             * The proxy returned by stringToProxy is of type Ice.ObjectPrx,
             * which is at the root of the inheritance tree for interfaces and classes.
             * But to actually talk to our printer, we need a proxy for a StreamerInterface interface, not an Object interface.
             * To do this, we need to do a down-cast by calling StreamerInterfacePrxHelper.checkedCast.
             * A checked cast sends a message to the server, effectively asking "is this a proxy for a StreamerInterface interface?"
             * If so, the call returns a proxy of type StreamerInterface;
             * otherwise, if the proxy denotes an interface of some other type, the call returns null.
             */
            StreamerInterfacePrx streamerInterface = StreamerInterfacePrxHelper.checkedCast(base);

            // We test that the down-cast succeeded and, if not, throw an error message that terminates the client.
            if (streamerInterface == null)
                throw new Error("Invalid proxy");

            // The ID is generated by the Portal
            String id = streamerInterface.addStream(key, name, "TCP", Inet4Address.getLocalHost().getHostName(), 6666, width, height, 400, keywords);

            System.out.println("ID = " + id);

            // Start ffmpeg
            // TODO(migafgarcia): mess with ffmpeg options
            Process proc = new ProcessBuilder("ffmpeg", "-i", video,  "-f", "mpegts", "tcp://127.0.0.1:8080?listen=1").start();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            System.out.println("stdout:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null)
                System.out.println(s);


            proc.waitFor();



            streamerInterface.deleteStream(id, key);


        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            status = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            status = 1;
        }


        System.exit(status);

    }


}