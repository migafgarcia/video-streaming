package client;

import Ice.Current;
import portal.StreamInfo;
import portal._NotificationDisp;

import java.util.Arrays;
import java.util.HashMap;

public class NotificationI extends _NotificationDisp {

    private HashMap<String,StreamInfo> streams;

    public NotificationI() {
        super();

        streams = new HashMap<>();
    }

    @Override
    public void streamAdded(StreamInfo streamInfo, Current __current) {
        // TODO(migafgarcia): check for stuff
        streams.put(streamInfo.id, streamInfo);
        printStreams();
    }

    @Override
    public void streamDeleted(String id, Current __current) {
        // TODO(migafgarcia): check for stuff
        streams.remove(id);
        printStreams();
    }

    private void printStreams() {
        System.out.println("\n\n\nCurrent streams:");
        streams.values().forEach(stream -> System.out.println(stream.name + " " + Arrays.toString(stream.keywords)));
    }
}
