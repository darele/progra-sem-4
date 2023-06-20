package info.kgeorgiy.ja.piche_kruz.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * This is a multithreading implementation of {@link ParallelMapper}.
 *
 * This class can be used as resource for many instances at the same time, it answers
 * to requests in FIFO order
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadList = new ArrayList<>();

    private final Queue<Runnable> taskQueue = new ArrayDeque<>();

    private Runnable getNextTask() throws InterruptedException {
        synchronized (taskQueue) {
            while (taskQueue.isEmpty()) {
                taskQueue.wait();
            }
            taskQueue.notify();
            return taskQueue.poll();
        }
    }

    private void add(Runnable task) {
        synchronized (taskQueue) {
            // :NOTE: не нужно
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    private static class AnswerHandler <R> {
        private int readyResultCounter;
        List<R> partialAns;

        private AnswerHandler (int lim) {
            readyResultCounter = 0;
            partialAns = new ArrayList<>(Collections.nCopies(lim, null));
        }

        private synchronized void setVal(int index, R val) {
            partialAns.set(index, val);
            readyResultCounter++;
            if (readyResultCounter == partialAns.size()) {
                notify();
            }
        }

        private synchronized List<R> getAns() throws InterruptedException{
            while (readyResultCounter < partialAns.size()) {
                wait();
            }
            return partialAns;
        }
    }

    /**
     * Main Constructor.
     * <p>It creates a Mapper that works with at most <code>threads</code> threads</p>
     *
     * @param threads - The desired number of threads with which Mapper should work
     */
    public ParallelMapperImpl(final int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads count must be positive");
        }

        for (int i = 0; i < threads; i++) {
            Thread temp = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        getNextTask().run();
                    }
                } catch (final InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            threadList.add(temp);
            temp.start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        AnswerHandler<R> answerHandler = new AnswerHandler<>(args.size());
        int j = 0;
        List<Runnable> tasks = new ArrayList<>();
        List<RuntimeException> exceptions = new ArrayList<>();
        for (T i : args) {
            final int index = j++;
            Runnable task = () -> {
                // :NOTE: что будет, если вылетет runtime exception
                try {
                    final R val = f.apply(i);
                    answerHandler.setVal(index, val);
                } catch (RuntimeException e) {
                    exceptions.add(e);
                }
            };
            tasks.add(task);
        }
        for (Runnable i : tasks) {
            add(i);
        }
        if (!exceptions.isEmpty()) {
            InterruptedException ex = new InterruptedException();
            for (Exception exception : exceptions) {
                ex.addSuppressed(exception);
            }
            throw ex;
        }
        return answerHandler.getAns();
    }

    @Override
    public void close() {
        for (Thread i : threadList) {
            i.interrupt();
        }
    }
}
