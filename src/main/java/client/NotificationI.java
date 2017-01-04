package client;

import Ice.Current;
import portal.StreamInfo;
import portal._NotificationDisp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NotificationI extends _NotificationDisp {

    // Maps ids to stream infos (for finding stream by id)
    private Map<String,StreamInfo> streams;

    // Maps keywords to stream infos (for searching)
    private Map<String,HashSet<StreamInfo>> keywords;


    public NotificationI(StreamInfo[] initialStreams) {
        super();

        streams = Collections.synchronizedMap(new HashMap<String,StreamInfo>(initialStreams.length));

        keywords = Collections.synchronizedMap(new HashMap<String,HashSet<StreamInfo>>(initialStreams.length));

        for(StreamInfo stream : initialStreams) {
            streams.put(stream.id, stream);

            for(String keyword : stream.keywords) {
                HashSet<StreamInfo> streamsWithKeyword = keywords.get(keyword);
                if(streamsWithKeyword == null)
                    keywords.put(keyword, new HashSet<>(Arrays.asList(stream)));
                else
                    streamsWithKeyword.add(stream);

            }
        }
    }

    @Override
    public void streamAdded(StreamInfo streamInfo, Current __current) {
        streams.put(streamInfo.id, streamInfo);

        for(String keyword : streamInfo.keywords) {
            HashSet<StreamInfo> streamsWithKeyword = keywords.get(keyword);
            if(streamsWithKeyword == null)
                keywords.put(keyword, new HashSet<>(Arrays.asList(streamInfo)));
            else
                streamsWithKeyword.add(streamInfo);

        }

    }

    @Override
    public void streamDeleted(String id, Current __current) {
        StreamInfo stream = streams.remove(id);
        for(String keyword : stream.keywords) {
            HashSet<StreamInfo> streamsWithKeyword = keywords.get(keyword);
            streamsWithKeyword.remove(stream);
            if(streamsWithKeyword.size() == 0)
                keywords.remove(keyword);

        }

    }

    @Override
    public void deleteAll(Current __current) {
        streams.clear();
        keywords.clear();

    }

    public Collection<StreamInfo> getStreams() {
        return streams.values();
    }

    public void search(String keyword) {
        if(keywords.containsKey(keyword) && keywords.get(keyword).size() > 0)
            keywords.get(keyword).forEach(stream -> System.out.println(stream.name + " " + Arrays.toString(stream.keywords)));
        else
            System.out.println("No streams match keyword " + keyword);
    }

    public StreamInfo getStreamInfo(String id) {
        return streams.get(id);
    }
}
