package tl.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.NotRegisteredException;
import com.zeroc.Ice.Properties;
import com.zeroc.Ice.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class MainActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView artistText;
    private ImageView albumImage;
    private TextView isRecordingText;
    private ImageButton recordButton;

    private boolean isRecording = false;
    Player player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titleText = findViewById(R.id.titleText);
        artistText = findViewById(R.id.artistText);
        albumImage = findViewById(R.id.albumImage);
        isRecordingText = findViewById(R.id.isRecording);
        recordButton = findViewById(R.id.recordButton);

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

        test();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void test() {
        titleText.setText("test");
        artistText.setText("test");
        new DownloadImageTask(albumImage).execute("https://picsum.photos/200");

        //play(false);
        playSong("Music-Sounds-Better-With-You.mp3");
    }

    private MediaRecorder recorder = null;

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
    }

    public static Context getContext() {
        return getContext();
    }

    public void playSong(String path) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player = null;

                try {
                    player = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player == null) {
                    System.err.println("couldn't find a `::Demo::Hello' object");
                } else {
                    player.playSong(path);
                    if(this.player == null) {
                        this.player = new Player();
                    }
                    this.player.start();
                }
            }
        }).start();
    }

    public void play(boolean play) {
        new Thread(() -> {
            InitializationData initData = new InitializationData();
            Properties properties = Util.createProperties();
            properties.setProperty("Ice.Default.Locator", "IceGrid/Locator:tcp -h 192.168.1.19 -p 12000");
            properties.setProperty("Ice.Trace.Network", "2");
            initData.properties = properties;

            try (Communicator communicator = Util.initialize(initData)) {
                communicator.getProperties().setProperty("Ice.Default.Package", "tl.app");

                System.out.println("trying to connect to player");
                PlayerCommandsPrx player = null;

                try {
                    player = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player1@Serv1.PlayerAdapter"));
                } catch (NotRegisteredException ex) {
                    System.out.println(ex.getMessage());
                }
                if (player == null) {
                    System.err.println("couldn't find a `::Demo::Hello' object");
                } else {
                    player.play(play);
                    if(this.player == null && play) {
                        this.player = new Player();
                        this.player.start();
                    }
                }
            }
        }).start();

    }
}