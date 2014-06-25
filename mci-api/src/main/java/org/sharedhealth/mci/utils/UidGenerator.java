package org.sharedhealth.mci.utils;

import java.nio.ByteBuffer;

import com.eaio.uuid.UUIDGen;

public class UidGenerator {

    private final long clockSeqAndNode = UUIDGen.getClockSeqAndNode();
    private final byte[] node = new byte[]{
            (byte)((clockSeqAndNode >> 40) & 0xff),
            (byte)((clockSeqAndNode >> 32) & 0xff),
            (byte)((clockSeqAndNode >> 24) & 0xff),
            (byte)((clockSeqAndNode >> 16) & 0xff),
            (byte)((clockSeqAndNode >> 8) & 0xff),
            (byte)((clockSeqAndNode) & 0xff),
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
        int maxShort = 0xff;
        if(seq == maxShort) {
            throw new RuntimeException("Too fast");
        }

        long time;
        synchronized(lock) {
            time = System.currentTimeMillis();
            if(time != lastTimestamp) {
                lastTimestamp = time;
                seq = 0;
            }
            seq++;
            ByteBuffer bb = bufferThreadLocal.get();
            bb.rewind();
            bb.putLong(time);
            bb.put(node);
            bb.putShort((short)seq);
            return bb.array();
        }
    }

    public String getId() {
        byte[] ba = getByteId();
        ByteBuffer bb = ByteBuffer.wrap(ba);
        long ts = bb.getLong();
        int node_0 = bb.getInt();
        short node_1 = bb.getShort();
        short seq = bb.getShort();
        System.out.write(node_0);
        return String.format("%012d%s%s%02d", ts, Integer.toHexString(node_0), Integer.toHexString(node_1), seq);
    }
}