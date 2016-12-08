module portal {
    sequence<string> stringArray;

    struct StreamInfo {
        string id;
        string name;
        string proto;
        string ip;
        int port;
        int width;
        int height;
        int bitrate;
        stringArray keywords;
    };

    struct Stream {
        string key;
        StreamInfo streamInfo;
    };

    sequence<StreamInfo> streamInfoList;

    interface ClientInterface {
        streamInfoList getStreams();
    };

    interface StreamerInterface {
        string addStream(string key, string name, string proto, string ip, int port, int width, int height, int bitrate, stringArray keywords);
        void deleteStream(string id, string key);
    };

    interface Notification {
        void streamAdded(StreamInfo streamInfo);
        void streamDeleted(string id);
    };
};