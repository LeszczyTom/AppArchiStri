package tl.app;


import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.widget.Toast;


import java.io.IOException;
import java.sql.SQLOutput;

public class MyService implements MediaPlayer.OnPreparedListener {
    MediaPlayer mediaPlayer = null;

    public void start() {
        String url = "http://10.0.2.2:8080/stream.mp3"; // your URL here
        mediaPlayer = new MediaPlayer();
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
        }
        mediaPlayer.setOnPreparedListener(this);
        System.out.println("Preparing media player");
        mediaPlayer.prepareAsync(); // prepare async to not block main thread
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        System.out.println("Media player is prepared");
        player.start();
    }

}