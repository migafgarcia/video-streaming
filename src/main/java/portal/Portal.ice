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

    struct ShortStreamInfo {
        string id;
        string name;
        stringArray keywords;
    };

    sequence<ShortStreamInfo> streamInfoList;


    interface ClientInterface {
        streamInfoList getStreams();
    };

    interface StreamerInterface {
        void addStream(StreamInfo streamInfo);
        void deleteStream(string id);
    };


};