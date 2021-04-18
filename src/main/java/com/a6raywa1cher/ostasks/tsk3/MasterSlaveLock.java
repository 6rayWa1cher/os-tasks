package com.a6raywa1cher.ostasks.tsk3;

import io.github.hcoona.concurrent.NamedMutex;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

public class MasterSlaveLock implements AutoCloseable {
//    private final FileLock fileLock;
    private final NamedMutex namedMutex;
    private final boolean isMaster;

    public MasterSlaveLock(String lockLocation) throws Exception {
//        File file = new File(lockLocation);
//        FileChannel channel = FileChannel.open(file.toPath(),
//                StandardOpenOption.CREATE, StandardOpenOption.DELETE_ON_CLOSE,
//                StandardOpenOption.WRITE, StandardOpenOption.APPEND);
//        channel.position(42);
//        this.fileLock = channel.tryLock();
//        if (!fileLock.isValid()) throw new Exception("File lock invalid");
        this.namedMutex = NamedMutex.newInstance(true, lockLocation);
        this.isMaster = namedMutex.waitOne(1, TimeUnit.MICROSECONDS);

    }

    public boolean isMaster() {
//        return fileLock != null;
        return isMaster;
    }

    @Override
    public void close() throws Exception {
        namedMutex.close();
    }
}
