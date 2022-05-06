package tl.app;


import android.media.AudioAttributes;
import android.widget.Toast;
import java.io.IOException;

public class Player implements Runnable {
    android.media.MediaPlayer mediaPlayer = null;

    public int start(String port) {
        //TODO: Modif pour le build en iplocalhost:port/stream.mp3
        String url = MainActivity.ADDRESS + port + "/stream.mp3"; // your URL here
        mediaPlayer = new android.media.MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            Toast.makeText(MainActivity.getContext(), "Erreur connexion stream", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return 0;
        }
        //mediaPlayer.setOnPreparedListener(this);
        System.out.println("Preparing media player");
        mediaPlayer.prepareAsync(); // prepare async to not block main thread
        return 1;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(android.media.MediaPlayer player) {
        System.out.println("Media player is prepared");
        player.start();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private String port;

    Player(String port) {
        this.port = port;
    }

    @Override
    public void run() {
        String url = MainActivity.ADDRESS + port + "/stream.mp3"; // your URL here
        mediaPlayer = new android.media.MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(MainActivity.getContext(), "Erreur connexion stream", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}