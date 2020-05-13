package fr.devnewton.hoerbertgui.io;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

public class HoerbertIO {

    public static void write(File[][] playlists, File sdcardPath) {
        for (int button = 0; button < playlists.length; ++button) {
            File folder = new File(sdcardPath, String.valueOf(button));
            folder.mkdirs();
            for (int song = 0; song < playlists[button].length; ++song) {
                writeSong(playlists[button][song], new File(folder, String.format("%d.wav", song)));
            }
        }
    }

    private static void writeSong(File inputSong, File outputWav) {
        try {
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le");
            audio.setChannels(1);
            audio.setSamplingRate(32000);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("wav");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(inputSong), outputWav, attrs);

        } catch (Exception ex) {
            Logger.getLogger(HoerbertIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
