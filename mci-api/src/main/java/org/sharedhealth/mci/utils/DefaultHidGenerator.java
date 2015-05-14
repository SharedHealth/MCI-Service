package org.sharedhealth.mci.utils;

import org.sharedhealth.mci.web.config.MCIProperties;
import org.sharedhealth.mci.web.exception.HidGenerationException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Qualifier("MciHidGenerator")
public class DefaultHidGenerator implements HidGenerator {

    private static final Logger logger = getLogger(DefaultHidGenerator.class);

    private static final int MAX_RETRY_COUNT = 50;

    static final long EPOCH_TIME = 1420070400000L; //2015-01-01T00:00:00.000Z
    private static final long MAX_SIGNED_LONG = 2199023255551L;
    private static final int MILLIS_IN_MIN = 60 * 1000;
    static final int WORKER_ID_BITS_SIZE = 3;
    static final int RANDOM_BITS_SIZE = 7;

    private long lastTimestamp = 0;
    private Set<Long> recentRandoms = new HashSet<>(500);

    private MCIProperties properties;
    private HidValidator hidValidator;
    private ChecksumGenerator checksumGenerator;

    @Autowired
    public DefaultHidGenerator(MCIProperties properties,
                               @Qualifier("MciHidValidator") HidValidator hidValidator,
                               @Qualifier("MciChecksumGenerator") ChecksumGenerator checksumGenerator) {
        this.properties = properties;
        this.hidValidator = hidValidator;
        this.checksumGenerator = checksumGenerator;
    }

    public synchronized String generate() {
        long id;
        int counter = 0;
        do {
            id = generateId();
            counter++;
        } while (!hidValidator.isValid(id) && counter < MAX_RETRY_COUNT);

        logger.debug("Retry counter: " + counter);

        if (!hidValidator.isValid(id)) {
            String message = format("Generated health id [%s] is not valid.", id);
            logger.debug(message);
            throw new HidGenerationException(message);
        }

        int checksum = this.checksumGenerator.generate(valueOf(id).substring(1));
        logger.debug("Checksum for id " + id + " is " + checksum);

        String generatedId = valueOf(id) + valueOf(checksum);
        logger.debug("Generated id: " + generatedId);
        return generatedId;
    }

    private long generateId() {
        long currentTimestamp = getCurrentTimestamp();
        logger.debug("Current timestamp since epoch in mins: " + currentTimestamp);

        long workerId = getWorkerId();
        logger.debug("Worker id: " + workerId);


        long random = generateRandomNumber();
        if (lastTimestamp != currentTimestamp) {
            // timestamp has advanced so reset it and clear the previous cache
            lastTimestamp = currentTimestamp;
            recentRandoms.clear();
        } else {
            // Same timestamp as previous keep generating randoms till new is found
            while (recentRandoms.contains(random)) {
                random = generateRandomNumber();
            }
        }
        recentRandoms.add(random);
        logger.debug("Random: " + random);

        BitSet idBits = BitSet.valueOf(new long[]{currentTimestamp << (WORKER_ID_BITS_SIZE + RANDOM_BITS_SIZE)});
        logger.debug("Timestamp bits with shift: " + toBinaryString(idBits));

        BitSet workerIdBits = BitSet.valueOf(new long[]{workerId << RANDOM_BITS_SIZE});
        logger.debug("Worker id bits with shift: " + toBinaryString(workerIdBits));
        idBits.or(workerIdBits);

        BitSet randomBits = BitSet.valueOf(new long[]{random});
        logger.debug("Random bits with shift: " + toBinaryString(randomBits));
        idBits.or(randomBits);

        long id = toLong(idBits) + getMinHidValue();
        logger.debug("Id w/o prefix and checksum: " + id);
        return id;
    }

    long getMinHidValue() {
        /**
         * One of the generated ids while implementing was 164041872.
         * 9636000000 + 164041872 = 9800041872.
         * And first several bits contain time component.
         * So generated ids will not be less than 164000000.
         */
        return 9636000000L;
    }

    private long getCurrentTimestamp() {
        long currentTimestamp = getCurrentTimeMins();

        while (lastTimestamp > currentTimestamp) {
            // Clock is running backwards so wait until it isn't
            currentTimestamp = getCurrentTimeMins();
        }

        long epochTimeMins = getEpochTimeMins();
        if (currentTimestamp < epochTimeMins || currentTimestamp > MAX_SIGNED_LONG) {
            String message = format("Current timestamp %s cannot be less than epoch %s or greater than %s.",
                    toIsoFormat(currentTimestamp), toIsoFormat(epochTimeMins), MAX_SIGNED_LONG);
            logger.debug(message);
            throw new HidGenerationException(message);
        }

        return currentTimestamp - epochTimeMins;
    }

    long getEpochTimeMins() {
        return EPOCH_TIME / MILLIS_IN_MIN;
    }

    long getCurrentTimeMins() {
        long currentTimeMillis = currentTimeMillis();
        return currentTimeMillis / MILLIS_IN_MIN;
    }

    long getWorkerId() {
        long workerId = Long.valueOf(properties.getWorkerId());
        long maxWorkerId = getMaxWorkerId();
        if (workerId < 0 || workerId > maxWorkerId) {
            String message = format("Worker id %s cannot be less than 0 or more than %s.", workerId, maxWorkerId);
            logger.debug(message);
            throw new HidGenerationException(message);
        }
        return workerId;
    }

    long getMaxWorkerId() {
        return round(pow(2, WORKER_ID_BITS_SIZE) - 1);
    }

    long generateRandomNumber() {
        long random = ThreadLocalRandom.current().nextLong();
        long maxRandom = getMaxRandomNumber();
        return Math.abs(random % (maxRandom + 1));
    }

    long getMaxRandomNumber() {
        return round(pow(2, RANDOM_BITS_SIZE) - 1);
    }

    private static Long toLong(BitSet bs) {
        long[] array = bs.toLongArray();
        if (array.length == 0) {
            return null;
        }
        return array[0];
    }

    private static String toBinaryString(BitSet bs) {
        Long value = toLong(bs);
        if (value == null) {
            return null;
        }
        return Long.toBinaryString(value);
    }
}