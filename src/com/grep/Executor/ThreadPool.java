package com.grep.Executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool implements ExecutorService {
    final int  capacity;
    volatile int currentCapacity=0;
    volatile boolean killCalled=false;
    AtomicInteger taskLeft=new AtomicInteger(0);
    LinkedBlockingQueue<Runnable> linkedBlockingQueue= new LinkedBlockingQueue<>();
    CountDownLatch killBlocker=new CountDownLatch(1);
    List<Thread> threads=new ArrayList<>();


    private class Task implements Runnable{
        @Override
        public void run() {
            while (!(Thread.currentThread().isInterrupted())) {
                try {
                    linkedBlockingQueue.take().run();
                    taskLeft.decrementAndGet();
                    if(killCalled && taskLeft.get()==0){
                        killBlocker.countDown();
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    private synchronized boolean checkThreads(){
        if(currentCapacity < capacity){
            currentCapacity++;
            return true;
        }
        return false;
    }
    public ThreadPool(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void submit(Runnable r) {
        taskLeft.incrementAndGet();
        linkedBlockingQueue.add(r);
        if (checkThreads()) {
            Thread t = new Thread(new Task());
            t.start();
            threads.add(t);
        }
    }

    @Override
    public void kill() {
        try{
            killCalled=true;
            if(taskLeft.get()!=0){
                killBlocker.await();
            }
            for(Thread t:threads){
                t.interrupt();
            }
        }catch( InterruptedException e){
            e.printStackTrace();
        }

    }
}
