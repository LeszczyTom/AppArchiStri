package tl.app;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RequestFromAPI {

    private static final String HOST = MainActivity.ADDRESS + "2222/";

    public static List<Song> getAllFromDb() {
        List<Song> songs = new ArrayList<>();
        System.out.println(HOST);
        try{
            URL url = new URL(HOST + "selectAllFromDB");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");

            // On lit la réponse
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            http.disconnect();

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("Items");

            for(int i = 0; i < jsonArray.length(); i++) {
                String album = jsonArray.getJSONObject(i).getJSONObject("Album").get("S").toString();
                String artist = jsonArray.getJSONObject(i).getJSONObject("Artist").get("S").toString();
                String duration = jsonArray.getJSONObject(i).getJSONObject("Duration").get("S").toString();
                String uri = jsonArray.getJSONObject(i).getJSONObject("URI").get("S").toString();
                String id = jsonArray.getJSONObject(i).getJSONObject("id").get("S").toString();
                String favorite = jsonArray.getJSONObject(i).getJSONObject("Favorite").get("S").toString();
                String serverId = jsonArray.getJSONObject(i).getJSONObject("ServerId").get("S").toString();
                String cover = jsonArray.getJSONObject(i).getJSONObject("Cover").get("S").toString();
                String songTitle = jsonArray.getJSONObject(i).getJSONObject("SongTitle").get("S").toString();
                songs.add(new Song(album, artist, duration, uri, id, favorite, serverId, cover, songTitle));
            }
            ;
        } catch (Exception e) {
            System.out.println("ERORRRRR ");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return songs;
    }
}
