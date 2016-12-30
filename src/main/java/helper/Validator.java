package helper;

/**
 *
 */
public class Validator {

    private static final String IP_REGEX =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean validateKey(String key) {
        return (key.length() == Md5.KEY_SIZE) || key.matches("[0-9a-z]+");
    }

    public static boolean validateProto(String proto) {
        return proto.toLowerCase().equals("tcp");
    }

    public static boolean validateIp(String ip) {
        return ip.matches(IP_REGEX);
    }

    public static boolean validateResolution(int width, int height) {
        return width >= 800 && width <= 1920 && height >= 600 && height <= 1080;
    }




}
