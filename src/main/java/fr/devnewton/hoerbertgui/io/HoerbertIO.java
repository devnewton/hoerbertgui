package fr.devnewton.hoerbertgui.io;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

public class HoerbertIO implements Serializable {

    private String sdcard;

    public static final String PROP_SDCARD = "sdcard";

    public String getSdcard() {
        return sdcard;
    }

    public void setSdcard(String sdcard) {
        String oldSdcard = this.sdcard;
        this.sdcard = sdcard;
        propertyChangeSupport.firePropertyChange(PROP_SDCARD, oldSdcard, sdcard);
    }

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void clearPlaylist() {
        try {
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.wav");
            Files.walkFileTree(Paths.get(sdcard), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(HoerbertIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writePlaylist(File[][] playlists) {
        if (null == sdcard) {
            return;
        }
        clearPlaylist();
        for (int button = 0; button < playlists.length; ++button) {
            File folder = new File(sdcard, String.valueOf(button));
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

    public Path findFileStoreRootPath(FileStore filestore) {
        try {
            for (Path root : FileSystems.getDefault().getRootDirectories()) {
                if (Files.isDirectory(root) && Files.getFileStore(root).equals(filestore)) {
                    return root;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HoerbertIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void detectSdcard() {
        for (FileStore filestore : FileSystems.getDefault().getFileStores()) {
            if (filestore.isReadOnly()) {
                continue;
            }
            Path path = findFileStoreRootPath(filestore);
            if(null != path && !Paths.get("C:\\").equals(path) && !Paths.get("/").equals(path)) {
                this.sdcard = path.toString();
            }
        }
    }
}
