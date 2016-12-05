package portal;

import Ice.Current;

import java.util.Arrays;

public class StreamerInterfaceI extends _StreamerInterfaceDisp {

    @Override
    public void addStream(StreamInfo streamInfo, Current __current) {
        System.out.println(
                "addStream(" +
                        "id: \"" + streamInfo.id +
                        "\", name: \"" + streamInfo.name +
                        "\", proto: \"" + streamInfo.proto +
                        "\", ip: \"" + streamInfo.ip +
                        "\", port: " + streamInfo.port +
                        ", width: " + streamInfo.width +
                        ", height: " + streamInfo.height +
                        ", bitrate: " + streamInfo.bitrate +
                        ", keywords: " + Arrays.toString(streamInfo.keywords) + ")");
    }

    @Override
    public void deleteStream(String id, Current __current) {
        System.out.println("closeStream(id: \"" + id + "\")");
    }
}
