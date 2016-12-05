package subscriber;

import Ice.Current;
import shared.Measurement;
import shared._MonitorDisp;

/**
 *
 */
public class MonitorI extends _MonitorDisp {


    @Override
    public void report(Measurement m, Current __current) {
        System.out.println(
                "Measurement report:\n" +
                        "  Tower: " + m.tower + "\n" +
                        "  W Spd: " + m.windSpeed + "\n" +
                        "  W Dir: " + m.windDirection + "\n" +
                        "   Temp: " + m.temperature + "\n");
    }
}
