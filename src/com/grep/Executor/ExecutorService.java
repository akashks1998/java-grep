package com.grep.Executor;

public interface ExecutorService {
    void submit(Runnable r);
    void kill() throws InterruptedException;
}
