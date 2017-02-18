package com.yo.shishkoam.recognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by User on 17.02.2017
 */

public class Utils {
    public static boolean saveFile(File file, byte[] bytes) {
        FileOutputStream outputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
        } catch (Exception e) {
            return false;
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }
}
