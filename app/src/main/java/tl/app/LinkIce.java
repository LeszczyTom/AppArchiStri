package tl.app;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.NotRegisteredException;
import com.zeroc.Ice.Util;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;

public class LinkIce {

    List<Song> songs;
    PlayerCommandsPrx player1;

    private void pause(){
        playPause(false);
    }

    private void play(){
        playPause(true);
    }
    private void playPause(boolean play) {
        try (Communicator communicator = Util.initialize(/*initData*/)) {

            System.out.println("trying to connect to player");
            player1 = null;

            try {
                System.out.println("trying to connect to player 1");
                player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player:default -h 167.172.187.86 -p 10000"));
                player1.play(play);
                if(play) {
                    System.out.println("Playing");
                } else {
                    System.out.println("Paused");
                }
            } catch (NotRegisteredException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private Song playRightSong(String object) {
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

        playSong(song.getUri());
        return song;
    }

    private void playSong(String song){

        try (Communicator communicator = Util.initialize(/*initData*/)) {

            System.out.println("trying to connect to player");
            player1 = null;

            try {
                System.out.println("trying to connect to player 1");
                player1 = PlayerCommandsPrx.checkedCast(communicator.stringToProxy("player:default -h 167.172.187.86 -p 10000"));
                player1.playSong(song);
                System.out.println("Playing song: " + song);

                if(MainActivity.player == null) {
                    MainActivity.player = new Player();
                }
                MainActivity.player.setMediaPlayer();

            } catch (NotRegisteredException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public Song action(Result result){
        if(result.getAction().equals("resume")){
            play();
        } else if(result.getAction().equals("pause")){
            pause();
        } else if(result.getAction().equals("play")){
            return playRightSong(result.getObjet());
        }
        return null;
    }
}
