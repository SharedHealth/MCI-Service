package org.sharedhealth.mci.utils;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String asString(String path) {
        try {
            return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        } catch (IOException e) {
            logger.error("Could not read file");
            throw new RuntimeException("File not found", e);
        }
    }

    public static void addHidToFile(File file, String hid) {
        try {
            String hidToWrite = hid + System.getProperty("line.separator");
            Files.append(hidToWrite, file, Charsets.UTF_8);
        } catch (IOException e) {
            logger.error("Can not write HID to file");
            e.printStackTrace();
        }
    }

    public static File createHIDFile(String hidStorageDirPath, String fileName) {
        File outputDir = new File(hidStorageDirPath);
        outputDir.mkdirs();
        return new File(outputDir, fileName);
    }
}
