package client;

import Ice.Current;
import portal.ShortStreamInfo;
import portal._NotificationDisp;

/**
 *
 */
public class NotificationI extends _NotificationDisp {
    @Override
    public void addStream(ShortStreamInfo streamInfo, Current __current) {
        System.out.println("STREAM ADDED:\n" + streamInfo.toString());
    }

    @Override
    public void deleteStream(String id, Current __current) {
        System.out.println("STREAM DELETED:\n id = " + id);
    }
}
