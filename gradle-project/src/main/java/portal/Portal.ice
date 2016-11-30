module portal {
    sequence<string> stringArray;

    struct StreamInfo {
        string name;
        string proto;
        string ip;
        int port;
        int width;
        int height;
        int bitrate;
        stringArray keywords;
    };

    struct ShortStreamInfo {
        string id;
        string name;
        stringArray keywords;
    };

    sequence<ShortStreamInfo> streamInfoList;

    interface StreamerInterface {
        string addStream(StreamInfo streamInfo);
        void closeStream(string id);
    };

    interface ClientInterface {
        streamInfoList getStreams();
    };
};