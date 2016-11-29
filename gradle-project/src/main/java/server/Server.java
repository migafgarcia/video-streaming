package server;

/**
 *
 */
public class Server {

    public static void main(String[] args) {
        int status = 0;

        Ice.Communicator ic = null;

        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectAdapter adapter =
                    ic.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 10000");
            Ice.Object object = new PrinterI();
            adapter.add(object, ic.stringToIdentity("SimplePrinter"));
            adapter.activate();
            ic.waitForShutdown();
        } catch(Ice.LocalException e) {

        }
        if (ic != null) {
            // Clean up
            //
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }

        System.exit(status);

    }


}
