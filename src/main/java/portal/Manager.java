package portal;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import Ice.Current;
import helper.Md5;
import helper.Validator;

/**
 * Manages Streamers and Clients
 */
public class Manager {

    private HashMap<String,Stream> streams;

    private NotificationPrx notificationPrx;

    private StreamerInterfaceI streamerInterface;

    private ClientInterfaceI clientInterface;

    public Manager(NotificationPrx notificationPrx) {

        this.streams = new HashMap<>();

        this.notificationPrx = notificationPrx;

        this.streamerInterface = new StreamerInterfaceI();

        this.clientInterface = new ClientInterfaceI();

        this.notificationPrx.deleteAll();

    }

    public StreamerInterfaceI getStreamerInterface() {
        return streamerInterface;
    }

    public ClientInterfaceI getClientInterface() {
        return clientInterface;
    }

    public StreamInfo[] getStreamInfos() {

        AtomicInteger i = new AtomicInteger(0);

        StreamInfo[] streamInfos = new StreamInfo[streams.values().size()];

        streams.values().forEach(stream ->  streamInfos[i.getAndIncrement()] = stream.streamInfo); // Java 8 sexy magic??

        return streamInfos;

    }


    /**
     * Interface for streamers
     */
    private class StreamerInterfaceI extends _StreamerInterfaceDisp {

        @Override
        public String addStream(String key, String name, String proto, String hostname, int port, String res, String bitrate, String[] keywords, Current __current) {

            if(!Validator.validateKey(key)) {
                System.out.println("ADD STREAM -> invalid key");
                return null;
            }

            if(!Validator.validateProto(proto)) {
                System.out.println("ADD STREAM -> invalid protocol");
                return null;
            }

            if(!Validator.validateResolution(res)) {
                System.out.println("ADD STREAM -> invalid resolution");
                return null;
            }

            // Print for logging
            System.out.println(
                    "ADD STREAM -> " +
                            "name: \"" + name +
                            "\", proto: \"" + proto +
                            "\", hostname: \"" + hostname +
                            "\", port: " + port +
                            ", res: " + res +
                            ", bitrate: " + bitrate +
                            ", keywords: " + Arrays.toString(keywords));

            // Generate id for new stream (expensive, but only run once)
            String id = Md5.md5(new ByteArrayInputStream(new String(
                    name +
                            proto +
                            hostname +
                            port +
                            res +
                            bitrate +
                            Arrays.toString(keywords) +
                            System.currentTimeMillis()).getBytes()));

            System.out.println("ID = " + id);

            if (streams.containsKey(id)) {
                System.out.println("ADD STREAM -> id: \"" + id + "\" failed: stream already added");
                return null;
            }

            StreamInfo streamInfo = new StreamInfo(id, name, proto, hostname, port, res, bitrate, keywords);

            // Adds the stream to the list
            streams.put(id, new Stream(key, streamInfo));

            // Inform icestorm server of new stream
            notificationPrx.streamAdded(streamInfo);

            return id;

        }

        @Override
        public void deleteStream(String id, String key, Current __current) {

            if(!Validator.validateKey(id) || !Validator.validateKey(key)) {
                System.out.println("DELETE STREAM -> invalid key or id"); // this isn't lazy, just practical
                return;
            }

            Stream stream = streams.get(id);


            if (stream == null)
                System.out.println("DELETE STREAM -> id: \"" + id + "\" failed: stream doesn't exist");
            else if (key.equals(stream.key)) {
                streams.remove(id);
                notificationPrx.streamDeleted(id);
                System.out.println("DELETE STREAM -> id: \"" + id + "\" successfully");
            } else
                System.out.println("DELETE STREAM -> id: \"" + id + "\" failed: unmatched keys");

        }
    }

    /**
     * Interface for clients
     */
    class ClientInterfaceI extends _ClientInterfaceDisp {

        @Override
        public StreamInfo[] getStreams(Current __current) {
            return getStreamInfos();
        }

    }

}
