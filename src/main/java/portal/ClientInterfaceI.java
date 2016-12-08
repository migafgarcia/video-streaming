package portal;

import Ice.Current;

/**
 *
 */
public class ClientInterfaceI extends _ClientInterfaceDisp {

    private StreamerInterfaceI streamerInterface;

    public ClientInterfaceI(StreamerInterfaceI streamerInterface) {
        super();
        this.streamerInterface = streamerInterface;
    }

    @Override
    public StreamInfo[] getStreams(Current __current) {
        return streamerInterface.getStreamInfos();
    }
}
