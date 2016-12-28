package client;

import Ice.Current;
import portal.StreamInfo;
import portal._NotificationDisp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NotificationI extends _NotificationDisp {

    private Map<String,StreamInfo> streams;

    public NotificationI(StreamInfo[] initialStreams) {
        super();

        streams = Collections.synchronizedMap(new HashMap<String,StreamInfo>());

        for(StreamInfo stream : initialStreams)
            streams.put(stream.id, stream);

        printStreams();

    }

    @Override
    public void streamAdded(StreamInfo streamInfo, Current __current) {
        // TODO(migafgarcia): check for stuff
        streams.put(streamInfo.id, streamInfo);
    }

    @Override
    public void streamDeleted(String id, Current __current) {
        // TODO(migafgarcia): check for stuff
        streams.remove(id);
    }

    @Override
    public void deleteAll(Current __current) {
        streams.clear();
    }

    public Map<String, StreamInfo> getStreams() {
        return streams;
    }

    public StreamInfo getStreamInfo(String id) {
        return streams.get(id);
    }

    public void printStreams() {
        if(streams.size() > 0) {
            System.out.println("Current streams:");
            streams.values().forEach(stream -> System.out.println(stream.id + " " + stream.name + " " + Arrays.toString(stream.keywords)));
        }
        else
            System.out.println("No streams available");
    }

}
