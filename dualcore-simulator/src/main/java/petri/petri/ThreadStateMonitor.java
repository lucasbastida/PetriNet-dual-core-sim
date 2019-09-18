package petri;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import verification.*;

public class ThreadStateMonitor {

    private static boolean finished = false;
    private static int counter = 0;
    private static Lock lock = new ReentrantLock();
    private static ReentrantReadWriteLock finishedLock = new ReentrantReadWriteLock();
    private static final int thr_total = 8;

    public static void add() {
        lock.lock();
        try{
            counter++;
            System.out.println(counter);
            if (counter == thr_total && isFinished()){
                DualCoreChart.init();
                TransitionInvariant.init();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void subtract() {
        lock.lock();
        try{
            counter--;
            System.out.println(counter);
            if (counter == thr_total && isFinished()){
                DualCoreChart.init();
                TransitionInvariant.init();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void setFinished() {
        finishedLock.writeLock().lock();
        try {
            finished = true;
        } finally {
            finishedLock.writeLock().unlock();
        }
    }

    public static boolean isFinished() {
        finishedLock.readLock().lock();
        try {
            return finished;
        } finally {
            finishedLock.readLock().unlock();
        }
    }
}
