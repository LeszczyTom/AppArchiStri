package tl.app;

public class IceClient {
    public IceClient(String[] args) {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs)) {
            communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceGrid.simple");

            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                status = 1;
            } else {
                status = run(communicator);
            }
        }
    }

    private static int run (com.zeroc.Ice.Communicator communicator){
        //
        // First we try to connect to the object with the `hello'
        // identity. If it's not registered with the registry, we
        // search for an object with the ::Demo::Hello type.
        //
        tl.PlayerCommandsPrx player = null;
        try {
            player = tl.PlayerCommandsPrx.checkedCast(communicator.stringToProxy("hello"));
        } catch (com.zeroc.Ice.NotRegisteredException ex) {
            System.out.println(ex.getMessage());
        }
        if (player == null) {
            System.err.println("couldn't find a `::Demo::Hello' object");
            return 1;
        }
        return 0;
    }
}
