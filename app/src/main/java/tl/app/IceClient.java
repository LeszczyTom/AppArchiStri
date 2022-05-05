package tl.app;


import android.os.AsyncTask;

import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.Properties;
import com.zeroc.Ice.Util;

public class IceClient extends AsyncTask<Void, Void, Void> {

    PlayerCommandsPrx player = null;

    @Override
    protected Void doInBackground(Void... voids) {
        InitializationData initData = new InitializationData();
        Properties properties = Util.createProperties();
        properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
        properties.setProperty("Ice.Trace.Network", "2");
        initData.properties = properties;

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(initData)) {
            communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

            System.out.println("trying to connect to player");

            try {
                player = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
            } catch (com.zeroc.Ice.NotRegisteredException ex) {
                System.out.println(ex.getMessage());
            }
            if (player == null) {
                System.err.println("couldn't find a `::Demo::Hello' object");
            }
        }
        return null;
    }

    public void play(boolean play) {
        player.play(play);
        Player myService = new Player();
        System.out.println(myService.start());
    }
}
