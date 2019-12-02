package com.automaton.http;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LengthPrefixedProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LengthPrefixedProcessor.class);

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private Byte firstLengthByteBuffer;
    private int targetLength = 0;

    public synchronized Collection<byte[]> handle(byte[] data) {
        Collection<byte[]> results = (Collection) new LinkedList<>();
        int pos = 0;
        logger.trace("Received message of length {}. Existing buffer is {}", Integer.valueOf(data.length),
                Integer.valueOf(this.buffer.size()));
        if (this.buffer.size() == 0) {
            while (data.length - pos > 18) {
                int targetLength = (data[0] & 0xFF) + (data[1] & 0xFF) * 256 + 16 + 2;
                logger.trace("Attempting to read message of length {}", Integer.valueOf(targetLength));
                if (data.length >= pos + targetLength) {
                    byte[] b = new byte[targetLength - 2];
                    System.arraycopy(data, pos + 2, b, 0, targetLength - 2);
                    results.add(b);
                    logger.trace("Read complete message");
                    pos += targetLength;
                    continue;
                }
                logger.trace("Not enough data available");
            }
        }

        if (data.length > pos) {
            logger.trace("Remaining data available");
            step(data, pos, results);
        }
        logger.trace("Returning {} results", Integer.valueOf(results.size()));
        return results;
    }

    private void step(byte[] data, int pos, Collection<byte[]> results) {
        logger.trace("Performing step operation on buffer of length {} with pos {}", Integer.valueOf(data.length),
                Integer.valueOf(pos));
        if (this.targetLength == 0 && data.length == 1 + pos) {
            this.firstLengthByteBuffer = Byte.valueOf(data[pos]);
            logger.trace("Received a single byte message, storing byte {} for later", this.firstLengthByteBuffer);
            return;
        }
        if (this.targetLength == 0) {
            if (this.firstLengthByteBuffer != null) {
                this.targetLength = (this.firstLengthByteBuffer.byteValue() & 0xFF) + (data[pos] & 0xFF) * 256 + 16;
                pos++;
                logger.trace("Received the second byte after storing the first byte. New length is {}",
                        Integer.valueOf(this.targetLength));
            } else {
                this.targetLength = (data[pos] & 0xFF) + (data[pos + 1] & 0xFF) * 256 + 16;
                pos += 2;
                logger.trace("targetLength is {}", Integer.valueOf(this.targetLength));
            }
        }
        int toWrite = this.targetLength - this.buffer.size();
        if (toWrite <= data.length - pos) {

            logger.trace("Received a complete message");
            this.buffer.write(data, pos, toWrite);
            results.add(this.buffer.toByteArray());
            this.buffer.reset();
            this.targetLength = 0;
            if (pos + toWrite > data.length) {
                step(data, pos + toWrite, results);
            }
        } else {
            logger.trace("Storing {} bytes in buffer until we receive the complete {}",
                    Integer.valueOf(data.length - pos), Integer.valueOf(this.targetLength));
            this.buffer.write(data, pos, data.length - pos);
        }
    }
}
