package streamer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;

import portal.StreamerInterfacePrx;
import portal.StreamerInterfacePrxHelper;
import utils.Md5;

/**
 *
 */
public class Streamer {

    public static void main(String[] args) {

        // Read arguments

        if(args.length < 4) {
            System.out.println("Wrong number of arguments:\n\t $ java streamer.Streamer [NAME] [VIDEO] [WIDTH] [HEIGHT] [KEYWORD]...");
            System.exit(0);
        }

        String name = args[0];
        String video = args[1];
        int width = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);
        String[] keywords = Arrays.copyOfRange(args, 4, args.length);
        String key = Md5.md5(new ByteArrayInputStream(new String(name + width + height + Arrays.toString(keywords) + System.currentTimeMillis()).getBytes()));
        int status = 0;

        try {

            Ice.Communicator ic = Ice.Util.initialize(args);

            Ice.ObjectPrx base = ic.stringToProxy("StreamerInterface:default -p 10000");

            StreamerInterfacePrx streamerInterface = StreamerInterfacePrxHelper.checkedCast(base);

            if (streamerInterface == null)
                throw new Error("Invalid proxy");

            String id = streamerInterface.addStream(key, name, "TCP", Inet4Address.getLocalHost().getHostName(), 6666, width, height, 400, keywords);

            for(int i = 0; i < 5 && id.equals(""); i++) {
                System.out.println("Stream refused. ");
                Thread.sleep(3000);
                System.out.println("Retrying...");
                id = streamerInterface.addStream(key, name, "TCP", Inet4Address.getLocalHost().getHostName(), 6666, width, height, 400, keywords);
            }

            System.out.println("ID = " + id);

            // ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", video, "-analyzeduration 500k", "-probesize 500k", "-r 30", "-c:v libx264", "-f mpegts", "-pix_fmt yuv420p", "tcp://127.0.0.1:8080?listen");
            // Process p = pb.start();
            // ffmpeg -i video.mp4 -analyzeduration 500k -probesize 500k -r 30 -c:v libx264 -f mpegts -pix_fmt yuv420p tcp://127.0.0.1:8080?listen
            // p.waitFor();

            //streamerInterface.deleteStream(id, key);


        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            status = 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            status = 1;
        }


        System.exit(status);

    }


}
