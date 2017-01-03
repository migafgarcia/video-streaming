package streamer;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import helper.Validator;
import portal.StreamerInterfacePrx;
import portal.StreamerInterfacePrxHelper;
import helper.Md5;
import shared.Resolutions;

/**
 * Streamer client
 */
public class Streamer {

    public static final int MAX_RETRY = 5;


    public static void main(String[] args) {

        // Read arguments
        if (args.length < 5) {
            System.out.println("Wrong number of arguments:\n\t $ java streamer.Streamer [PORTAL_URL] [NAME] [VIDEO] [RESOLUTION] [KEYWORD]...");
            System.exit(0);
        }

        // The URL of the Portal
        String portalUrl = args[0];

        // The name that will appear on the Portal
        String name = args[1];

        // The video file
        String video = args[2];

        // The width in pixels
        String res = args[3];

        // Keywords defining the stream
        String[] keywords = Arrays.copyOfRange(args, 4, args.length);

        // Key generated that will allow a Streamer to perform actions on the Portal concerning his stream
        String key = Md5.md5(new ByteArrayInputStream(new String(name + res + Arrays.toString(keywords) + System.currentTimeMillis()).getBytes()));

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


            /*
             * FFMPEG OPTIONS
             */

            /*
             * Read input at native frame rate. Mainly used to simulate a grab device, or live input stream (e.g. when reading from a file).
             * Should not be used with actual grab devices or live input streams (where it can cause packet loss).
             * By default ffmpeg attempts to read the input(s) as fast as possible.
             * This option will slow down the reading of the input(s) to the native frame rate of the input(s).
             * It is useful for real-time output (e.g. live streaming).
             */
            String READ_OPTION = "-re";

            String INPUT_OPTION = "-i";
            String INPUT = video;

            /*
             * Specify how many microseconds are analyzed to probe the input.
             * A higher value will enable detecting more accurate information, but will increase latency.
             * It defaults to 5,000,000 microseconds = 5 seconds.
             */
            String ANALYZE_DURATION_OPTION = "-analyzeduration";
            String ANALYZE_DURATION = "500k";

            /*
             * Set probing size in bytes, i.e. the size of the data to analyze to get stream information.
             * A higher value will enable detecting more information in case it is dispersed into the stream, but will increase latency.
             * Must be an integer not lesser than 32. It is 5000000 by default.
             */
            String PROBE_SIZE_OPTION = "-probesize";
            String PROBE_SIZE = "500k";

            String RATE_OPTION = "-r";
            String RATE = "30";

            String VIDEO_CODEC_OPTION = "-c:v";
            String VIDEO_CODEC = "libx264";

            String VIDEO_BITRATE_OPTION = "-b:v";
            String VIDEO_BITRATE = Resolutions.BITRATES.get(res);

            String BUF_SIZE_OPTION = "-bufsize";
            String BUF_SIZE = VIDEO_BITRATE;

            String MIN_BITRATE_OPTION = "-minrate";
            String MIN_BITRATE = VIDEO_BITRATE;

            String MAX_BITRATE_OPTION = "-maxrate";
            String MAX_BITRATE = VIDEO_BITRATE;

            String AUDIO_CODEC_OPTION = "-c:a";
            String AUDIO_CODEC = "libvo_aacenc";

            String AUDIO_BITRATE_OPTION = "-b:a";
            String AUDIO_BITRATE = "64k";

            /*
             * Set pixel format. Use -pix_fmts to show all the supported pixel formats.
             * If the selected pixel format can not be selected, ffmpeg will print a warning and select the best pixel format supported by the encoder.
             * If pix_fmt is prefixed by a +, ffmpeg will exit with an error if the requested pixel format can not be selected,
             * and automatic conversions inside filtergraphs are disabled.
             * If pix_fmt is a single +, ffmpeg selects the same pixel format as the input (or graph output) and automatic conversions are disabled.
             */
            String PIXEL_FORMAT_OPTION = "-pix_fmt";
            String PIXEL_FORMAT = "yuv420p";

            String FORMAT_OPTION = "-f";
            String FORMAT = "mpegts";

            String TUNE_OPTION = "-tune";
            String TUNE = "zerolatency";

            String PRESET_OPTION = "-preset";
            String PRESET = "veryfast";

            String VIDEO_PROFILE_OPTION = "-profile:v";
            String VIDEO_PROFILE = "baseline";


            String METADATA_OPTION = "-metadata";
            String METADATA = "title=\"" + name + "\"";

            String SCALE_OPTION = "-s";
            String SCALE = Resolutions.RESOLUTIONS.get(res);

            String OUTPUT = "tcp://127.0.0.1:8080?listen=1";

            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "ffmpeg",
                            READ_OPTION,
                            INPUT_OPTION, INPUT,
                            VIDEO_CODEC_OPTION, VIDEO_CODEC,
                            PIXEL_FORMAT_OPTION, PIXEL_FORMAT,
                            VIDEO_BITRATE_OPTION, VIDEO_BITRATE,
                            MIN_BITRATE_OPTION, MIN_BITRATE,
                            MAX_BITRATE_OPTION, MAX_BITRATE,
                            BUF_SIZE_OPTION, BUF_SIZE,
                            PRESET_OPTION, PRESET,
                            VIDEO_PROFILE_OPTION, VIDEO_PROFILE,
                            METADATA_OPTION, METADATA,
                            SCALE_OPTION, SCALE,
                            FORMAT_OPTION, FORMAT,
                            OUTPUT);


                    System.out.println(String.join(" ", pb.command()));
                    Process proc = pb.start();


                    BufferedReader stderr = new BufferedReader(new
                            InputStreamReader(proc.getErrorStream()));

                    String s = null;

                    while ((s = stderr.readLine()) != null)
                        System.out.println(s);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            //Establish a connection to ffmpeg
            SocketChannel videoSource = null;

            // Because ffmpeg takes a while to load, we retry MAX_RETRY times with 1 second intervals
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    videoSource = SocketChannel.open();
                    videoSource.connect(new InetSocketAddress("127.0.0.1", 8080));
                    break;
                } catch (ConnectException e) {
                    System.out.println("Connecting to ffmpeg, try " + i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            if (!videoSource.isConnected()) {
                System.out.println("Could not connect to ffmpeg");
                System.exit(1);
            }

            // Allocate buffer to store messages
            ByteBuffer buffer = ByteBuffer.allocate(1400);

            // Open a server socket channel for incoming requests
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(0));
            serverSocketChannel.configureBlocking(false);

            // The ID is generated by the Portal
            String id = streamerInterface.addStream(key, name, "TCP", InetAddress.getLocalHost().getHostName(), serverSocketChannel.socket().getLocalPort(), res, VIDEO_BITRATE, keywords);

            if (!Validator.validateKey(id)) {
                System.exit(1);
            }

            System.out.println("ID = " + id);
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> streamerInterface.deleteStream(id, key)));

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);

            while (videoSource.read(buffer) != -1) {

                // Non-blocking version of select
                selector.selectNow();

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    buffer.flip();
                    SelectionKey current = keyIterator.next();

                    if (current.isAcceptable()) {
                        SocketChannel channel = serverSocketChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_WRITE);
                    } else if (current.isWritable()) {
                        SocketChannel channel = (SocketChannel) current.channel();
                        try {
                            channel.write(buffer);
                        } catch (IOException e) {
                            current.cancel();
                        }
                    }

                    keyIterator.remove();
                }
                buffer.clear();
            }

        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            status = 1;
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            status = 1;
        } catch (IOException e) {
            e.printStackTrace();
            status = 1;
        }

        System.exit(status);

    }


}
