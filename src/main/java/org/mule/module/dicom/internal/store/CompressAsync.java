/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.store;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CompressAsync implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(CompressAsync.class);

    private final BlockingQueue<String> buffer;
    private final Path compressedFile;
    private volatile boolean finished = false;
    private volatile boolean running;
    public boolean isRunning() { return running; }

    private Exception lastException = null;
    public Exception getLastException() { return lastException; }

    public CompressAsync(String compressedFilename) {
        this.buffer = new LinkedBlockingQueue<>(10000);
        this.compressedFile = Paths.get(compressedFilename);
    }

    public void add(String filename) {
        buffer.add(filename);
    }

    public void setFinished() {	finished = true; }

    @Override
    public void run() {
        running = true;
        try (OutputStream fOut = Files.newOutputStream(compressedFile, StandardOpenOption.CREATE_NEW)) {
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(fOut);
            try (TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {
                while (!(finished && buffer.isEmpty())) {
                    String filename = buffer.poll(10, TimeUnit.MICROSECONDS);
                    if (filename != null) {
                        Path f = Paths.get(filename);
                        TarArchiveEntry entry = new TarArchiveEntry(f.toFile(), f.getFileName().toString());
                        tOut.putArchiveEntry(entry);
                        try (InputStream fIn = Files.newInputStream(f, StandardOpenOption.DELETE_ON_CLOSE)) {
                            long sizeWritten = IOUtils.copy(fIn, tOut);
                            fOut.flush();
                            entry.setSize(sizeWritten);
                            tOut.closeArchiveEntry();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            lastException = e;
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
            t.interrupt();
        } catch (IOException e) {
            lastException = e;
            log.error(e.toString(), e);
        } finally {
            finished = true;
            running = false;
        }
    }
}
