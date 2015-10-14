package ch.ethz.inf.vs.lubu.cyrptdbmodule;

import android.os.Environment;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lukas on 22.03.15.
 * Stores files on external storage (for TestData)
 */
public class FileUtil {

    private File file;

    public FileUtil(String filename) {
        if (!checkAvailable())
            throw new RuntimeException("External Stroage not available");
        this.file = getFile(filename);
        if (file.exists())
            file.delete();
    }

    public void writeToFile(InputStream dataStream) {
        FileOutputStream fw = null;
        try {
            fw = new FileOutputStream(file);
            ByteStreams.copy(dataStream, fw);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToFile(String message) {
        FileOutputStream fw = null;
        try {
            fw = new FileOutputStream(file);
            fw.write(message.getBytes());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean checkAvailable() {
        return isExternalStorageWritable() && isExternalStorageReadable();
    }

    private File getFile(String name) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), name);
        return file;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
