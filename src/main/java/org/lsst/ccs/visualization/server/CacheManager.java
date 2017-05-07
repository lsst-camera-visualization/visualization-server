package org.lsst.ccs.visualization.server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tonyj
 */
public class CacheManager implements Closeable {

    private final static Logger logger = Logger.getLogger(CacheManager.class.getName());
    private final FileStore fileStore;
    private final Path cachePath;
    private final long freeSpaceTarget;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CacheManager(File dir, long freeSpaceTarget) throws IOException {
        this.freeSpaceTarget = freeSpaceTarget;
        cachePath = dir.toPath();
        fileStore = Files.getFileStore(dir.toPath());
        logger.info(String.format("Directory %s, totalSpace=%,d unallocatedSpece=%,d usableSpace=%,d\n", dir,
                fileStore.getTotalSpace(), fileStore.getUnallocatedSpace(), fileStore.getUsableSpace()));
        makeSpace(freeSpaceTarget);
    }

    public void start(Duration scanPeriod) {

        scheduler.scheduleAtFixedRate(this::makeSpace, scanPeriod.getSeconds(), scanPeriod.getSeconds(), TimeUnit.SECONDS);
    }

    public void scanNow() {
        scheduler.execute(this::makeSpace);
    }

    private void makeSpace() {
        try {
            makeSpace(freeSpaceTarget);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to meet free space target", ex);
        }
    }

    private void makeSpace(long size) throws IOException {
        if (size < fileStore.getUsableSpace()) {
            return;
        }
        if (size > fileStore.getTotalSpace()) {
            throw new RuntimeException(String.format("Filestore not big enough to free %,d bytes", size));
        }
        SortedSet<FileWithCreationDate> files = scanFiles();
        for (FileWithCreationDate file : files) {
            Files.delete(file.path);
            logger.log(Level.INFO, "Deleting file {0} free space {1}", new Object[]{file, fileStore.getUsableSpace()});
            if (fileStore.getUsableSpace() >= size) {
                break;
            }
        }
    }

    private SortedSet<FileWithCreationDate> scanFiles() throws IOException {
        final SortedSet<FileWithCreationDate> files = new TreeSet<>();
        Files.walkFileTree(cachePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isDirectory()) {
                    files.add(new FileWithCreationDate(file, attrs));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            IOException x = new InterruptedIOException("Error while shutting down cache scheduler");
            x.initCause(ex);
            throw x;
        }
    }

    private static class FileWithCreationDate implements Comparable<FileWithCreationDate> {

        private final FileTime creationTime;
        private final Path path;
        private final long size;

        private FileWithCreationDate(Path file, BasicFileAttributes attrs) {
            this.path = file;
            this.creationTime = attrs.creationTime();
            this.size = attrs.size();
        }

        @Override
        public int compareTo(FileWithCreationDate other) {
            // This sorts oldest first
            return this.creationTime.compareTo(other.creationTime);
        }

        @Override
        public String toString() {
            return "FileWithCreationDate{" + "creationTime=" + creationTime + ", file=" + path + ", size=" + size + '}';
        }

    }

    public static void main(String args[]) throws IOException, InterruptedException {
        try (CacheManager cacheManager = new CacheManager(new File("/mnt/ramdisk/"), 2_000_000_000)) {
            SortedSet<FileWithCreationDate> scanFiles = cacheManager.scanFiles();
            scanFiles.forEach((f) -> {
                System.out.println(f);
            });
            cacheManager.start(Duration.ofSeconds(10));
            Thread.sleep(60_000);
        }
    }
}
