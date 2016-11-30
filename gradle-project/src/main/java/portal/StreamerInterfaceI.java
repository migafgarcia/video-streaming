package portal;

import Ice.Current;

import java.util.Arrays;

public class StreamerInterfaceI extends _StreamerInterfaceDisp {

    @Override
    public String addStream(StreamInfo streamInfo, Current __current) {
        System.out.println(
                "addStream(" +
                        "name: \"" + streamInfo.name +
                        "\", proto: \"" + streamInfo.proto +
                        "\", ip: \"" + streamInfo.ip +
                        "\", port: " + streamInfo.port +
                        ", width: " + streamInfo.width +
                        ", height: " + streamInfo.height +
                        ", bitrate: " + streamInfo.bitrate +
                        ", keywords: " + Arrays.toString(streamInfo.keywords) + ")");
        return "ABC123";
    }

    @Override
    public void closeStream(String id, Current __current) {
        System.out.println("closeStream(id: \"" + id + "\")");
    }
}
