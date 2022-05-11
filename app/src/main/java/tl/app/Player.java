package tl.app;


import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.io.IOException;

public class Player {
    String url = MainActivity.ADDRESS + "5555/stream.mp3"; // your URL here

    public void setMediaPlayer() {
        ExoPlayer player = new ExoPlayer.Builder(MainActivity.getContext()).build();
        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        // Set the media item to be played.
        player.setMediaItem(mediaItem);
        // Prepare the player.
        player.prepare();
        // Start the playback.
        player.play();
    }
}