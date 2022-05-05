module tl
{
    interface PlayerCommands {
        bool play(bool b);
        bool playSong(string song);
        bool stop();
        bool repeat(bool b);
        bool volume(int b);
        void downloadFile(string song);
        void removeFile(string song);
    };
};
