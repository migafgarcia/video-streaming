package shared;

import java.util.HashMap;

/**
 *
 */
public class Resolutions {
    public static final HashMap<String, String> BITRATES;
    public static final HashMap<String, String> RESOLUTIONS;

    static {
        BITRATES = new HashMap<>(7);
        BITRATES.put("240p", "640k");
        BITRATES.put("360p", "96k");
        BITRATES.put("432p", "1150k");
        BITRATES.put("480p", "1280k");
        BITRATES.put("576p", "1920k");
        BITRATES.put("720p", "2560k");
        BITRATES.put("1080p", "5120k");

        RESOLUTIONS = new HashMap<>(7);
        RESOLUTIONS.put("240p", "424x240");
        RESOLUTIONS.put("360p", "640x360");
        RESOLUTIONS.put("432p", "768x432");
        RESOLUTIONS.put("480p", "848x480");
        RESOLUTIONS.put("576p", "1024x576");
        RESOLUTIONS.put("720p", "1280x720");
        RESOLUTIONS.put("1080p", "1920x1080");

    }
}
