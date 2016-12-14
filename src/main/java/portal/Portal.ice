module portal {
    sequence<string> StringArray;

    struct StreamInfo {
        string id;
        string name;
        string proto;
        string ip;
        int port;
        int width;
        int height;
        int bitrate;
        StringArray keywords;
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
        string addStream(string key, string name, string proto, string ip, int port, int width, int height, int bitrate, StringArray keywords);
        void deleteStream(string id, string key);
    };

    interface Notification {
        void streamAdded(StreamInfo streamInfo);
        void streamDeleted(string id);
    };
};