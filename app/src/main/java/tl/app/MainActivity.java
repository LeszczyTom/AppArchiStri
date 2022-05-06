package tl.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.NotRegisteredException;
import com.zeroc.Ice.Properties;
import com.zeroc.Ice.Util;
import java.io.IOException;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class MainActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView artistText;
    private ImageView albumImage;
    private TextView isRecordingText;
    private ImageButton recordButton;
    private Button play;

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private Player songPlayer;
    private int currentServer = 0;
    private int volume = 50;

    private MediaRecorder recorder = null;

    private List<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        titleText = findViewById(R.id.titleText);
        artistText = findViewById(R.id.artistText);
        albumImage = findViewById(R.id.albumImage);
        isRecordingText = findViewById(R.id.isRecording);
        recordButton = findViewById(R.id.recordButton);
        play = findViewById(R.id.play);

        play.setOnClickListener(v -> {
            Result res = new Result ("stop");
            processAction(res);
            isPlaying = !isPlaying;
        });

        recordButton.setOnClickListener(v -> {
            if (!isRecording) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
                } else {
                    isRecordingText.setText(R.string.isRecording);
                    startRecording();
                    isRecording = true;
                }
            } else {
                isRecordingText.setText("");
                stopRecording();
                isRecording = false;
            }
        });

        songs = RequestFromAPI.getAllFromDb();

        updateUI(songs.get(0));


        Result res = new Result ("play", "Digital Love");
        //Result res = new Result("pause");
        processAction(res);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void processAction(Result res) {
        if ("play".equals(res.getAction())) {
            playRightSong(res.getObet());
        } else if ("pause".equals(res.getAction())) {
            play(false, currentServer);
        } else if ("volumedown".equals(res.getAction())) {
            volume -= 10;
            if (volume < 0) volume = 0;
            changeVolume(volume, currentServer);
        } else if ("resume".equals(res.getAction())) {
            play(true, currentServer);
        } else if ("volumeup".equals(res.getAction())) {
            volume += 10;
            if (volume > 100) volume = 100;
            changeVolume(volume, currentServer);
        } else if ("forward".equals(res.getAction())) {
            System.out.println("forward");
        } else if ("backward".equals(res.getAction())) {
            System.out.println("backward");
        } else if ("stop".equals(res.getAction())) {
            stop(currentServer);
        }
    }

    private void updateUI(Song song) {
        System.out.println("Song: " + song.toString());
        String title = song.getSongTitle();
        String artist = song.getArtist() + " - " + song.getAlbum();
        String image = song.getCover();

        titleText.setText(title);
        artistText.setText(artist);
        new DownloadImageTask(albumImage).execute(image);
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp");
        System.out.println(getExternalCacheDir().getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        //send to voice
    }

    public static Context getContext() {
        return getContext();
    }

    public void playRightSong(String object) {
        LevenshteinDistance ld = new LevenshteinDistance();
        int minDistance = Integer.MAX_VALUE;
        Song song = songs.get(0);

        for(Song s : songs) {
            int tmp = ld.apply(object, s.getSongTitle());
            if(tmp < minDistance) {
                song = s;
                minDistance = tmp;
            }
        }
        currentServer = Integer.parseInt(song.getServerId());
        playSong(song.getUri(), Integer.parseInt(song.getServerId()));
        updateUI(song);
    }

    public void playSong(String path, int serverId) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player1 = null;
                PlayerCommandsPrx player2 = null;

                try {
                    player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                    player2 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player2@Serv2.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player1 == null && player2 == null) {
                    System.err.println("error: invalid proxy");
                }

                if(serverId == 0) {
                    if (player1 != null) player1.playSong(path);
                    if (player2 != null) player2.play(false);
                } else {
                    if (player2 != null) player2.playSong(path);
                    if (player1 != null) player1.play(false);
                }

                if(songPlayer != null) songPlayer.stop();
                songPlayer = new Player();

                if(serverId == 0) {
                    songPlayer.start("1245");
                } else {
                    songPlayer.start("1246");
                }
                isPlaying = true;
                communicator.destroy();
            }
        }).start();
    }

    public void play(boolean play, int serverId) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player1 = null;
                PlayerCommandsPrx player2 = null;

                try {
                    player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                    player2 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player2@Serv2.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player1 == null || player2 == null) {
                    System.err.println("error: invalid proxy");
                } else {
                    if(serverId == 0) player1.play(play);
                    else player2.play(play);

                    isPlaying = play;
                    if(songPlayer == null && play) {
                        songPlayer = new Player();
                        if(serverId == 0) {
                            songPlayer.start("1245");
                        } else {
                            songPlayer.start("1246");
                        }
                    }
                }
                communicator.destroy();
            }
        }).start();
    }

    public void stop(int serverId) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player1 = null;
                PlayerCommandsPrx player2 = null;

                try {
                    player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                    player2 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player2@Serv2.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player1 == null || player2 == null) {
                    System.err.println("error: invalid proxy");
                } else {
                    if(serverId == 0) player1.stop();
                    else player2.stop();

                }
                communicator.destroy();
            }
        }).start();
    }

    public void changeVolume(int volume, int serverId) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player1 = null;
                PlayerCommandsPrx player2 = null;

                try {
                    player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                    player2 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player2@Serv2.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player1 == null || player2 == null) {
                    System.err.println("error: invalid proxy");
                } else {
                    if(serverId == 0) player1.volume(volume);
                    else player2.volume(volume);
                }
                communicator.destroy();
            }
        }).start();
    }
}