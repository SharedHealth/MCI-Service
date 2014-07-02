package org.sharedhealth.mci.utils;

import java.nio.ByteBuffer;

import static java.lang.System.getenv;

public class UidGenerator {

    public static final int EPOCH_TIME = 1325376000;

    private final long MCI_NODE = Long.parseLong(getenv("MCI_NODE"));

    private final byte[] node = new byte[]{
            (byte) ((MCI_NODE >> 40) & 0xff),
            (byte) ((MCI_NODE >> 32) & 0xff),
            (byte) ((MCI_NODE >> 24) & 0xff),
            (byte) ((MCI_NODE >> 16) & 0xff),
            (byte) ((MCI_NODE >> 8) & 0xff),
            (byte) ((MCI_NODE) & 0xff),
    };
    private final ThreadLocal<ByteBuffer> bufferThreadLocal = new ThreadLocal<ByteBuffer>() {
        @Override
        public ByteBuffer initialValue() {
            return ByteBuffer.allocate(16);
        }
    };

    private volatile int seq;
    private volatile long lastTimestamp;
    private final Object lock = new Object();

    public byte[] getByteId() {
        int maxShort = 0xffff;
        if (seq == maxShort) {
            throw new RuntimeException("Too fast");
        }

        long time;
        synchronized (lock) {
            time = System.currentTimeMillis() - EPOCH_TIME;
            if (time != lastTimestamp) {
                lastTimestamp = time;
                seq = 0;
            }
            seq++;
            ByteBuffer bb = bufferThreadLocal.get();
            bb.rewind();
            bb.putLong(time);
            bb.put(node);
            bb.putShort((short) seq);
            return bb.array();
        }
    }

    public String getId() {
        byte[] ba = getByteId();
        ByteBuffer bb = ByteBuffer.wrap(ba);
        long ts = bb.getLong();
        int node_0 = bb.getInt();
        short seq = bb.getShort();

        long value = (ts << 22) | (node_0 << 12) | seq;

        return Long.toString(value);
    }
}