package org.sharedhealth.mci.utils;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.valueOf;

public class ICDDRBHidGenerator {

    private static final long MIN_ID = 9100000000L;
    private static final long MAX_ID = 9799999999L;
    private static final long ID_COUNT = 1000000L;
    private static final String NEW_LINE_CHAR = "\n";
    private static final String FILE_NAME = "icddrb-hids.csv";

    private HidValidator hidValidator;
    private ChecksumGenerator checksumGenerator;

    public ICDDRBHidGenerator(HidValidator hidValidator, ChecksumGenerator checksumGenerator) {
        this.hidValidator = hidValidator;
        this.checksumGenerator = checksumGenerator;
    }

    public Set<String> generate(long count) {
        Set<String> ids = new HashSet<>();
        RandomDataGenerator randomData = new RandomDataGenerator();
        int checksum;

        while (ids.size() < count) {
            long id = randomData.nextLong(MIN_ID, MAX_ID);

            if (hidValidator.isValid(id)) {
                checksum = this.checksumGenerator.generate(valueOf(id).substring(1));
                ids.add(valueOf(id) + valueOf(checksum));
            }
        }
        return ids;
    }

    public void print(Set<String> ids) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(FILE_NAME);
            for (String id : ids) {
                fileWriter.append(valueOf(id)).append(NEW_LINE_CHAR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HidValidator hidValidator = new ICDDRBHidValidator();
        ChecksumGenerator checksumGenerator = new LuhnChecksumGenerator();
        ICDDRBHidGenerator hidGenerator = new ICDDRBHidGenerator(hidValidator, checksumGenerator);

        Set<String> ids = hidGenerator.generate(ID_COUNT);
        hidGenerator.print(ids);
    }
}
