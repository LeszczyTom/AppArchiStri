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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.NotRegisteredException;
import com.zeroc.Ice.Properties;
import com.zeroc.Ice.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.JSONObject;
import org.junit.Test;

public class MainActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView artistText;
    private ImageView albumImage;
    private TextView isRecordingText;
    private ImageButton recordButton;
    private EditText requeteText;
    private Button requeteButton;

    private boolean isRecording = false;
    private boolean isPlaying = false;

    public static Context context;
    private MediaRecorder recorder = null;

    public static Player player = null;

    private List<Song> songs;

    public static final String ADDRESS = "http://167.172.187.86:";
    LinkIce linkIce = new LinkIce();
    NLP nlp = new NLP();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        titleText = findViewById(R.id.titleText);
        artistText = findViewById(R.id.artistText);
        albumImage = findViewById(R.id.albumImage);
        isRecordingText = findViewById(R.id.isRecording);
        recordButton = findViewById(R.id.recordButton);
        requeteText = findViewById(R.id.requeteText);
        requeteButton = findViewById(R.id.requeteBtn);

        requeteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Result res = nlp.coupleActionObjetDepuisPhrase(requeteText.getText().toString());
                Song tmp = linkIce.action(res);
                if (tmp != null) updateUI(tmp);
            }
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
        linkIce.songs = songs;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
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

        voiceToText();
        //send to voice
    }

    public static Context getContext() {
        return context;
    }

    public void voiceToText() {
        try{
            URL url = new URL("http://pedago.univ-avignon.fr:3147/speechToText");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setDoOutput(true);


            Path path = Paths.get(getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp");
            byte[] audio = new byte[0];

            try {
                audio = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String audioByteString = Base64.getEncoder().encodeToString(audio);

            String jsonString = new JSONObject()
                    .put("audioEncoded", audioByteString)
                    .toString();

            System.out.println(jsonString);

            try(OutputStream os = http.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            http.connect();

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response);

                String[] result = response.toString().split("\\\\");
                String actionObjet = result[0].substring(1, result[0].length() - 1);
                System.out.println(actionObjet);
                String[] ao = actionObjet.split(",");
                String action = ao[0];
                String objet = ao[1].substring(0, ao[1].length() - 1);

                isRecordingText.setText(result[1]);
                Song tmp = linkIce.action(new Result(action, objet));
                if (tmp != null) updateUI(tmp);
            }
            http.disconnect();

        } catch (Exception e) {
            System.out.println("ERORRRRR ");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}