package info.kgeorgiy.ja.piche_kruz.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class implementing {@link ScalarIP}.
 *
 * It contains multithreading implementation for all methods
 */
public class IterativeParallelism implements ScalarIP {

    private final ParallelMapper map;

    /**
     * Default constructor.
     *
     * For simple instantiation and inheritance
     */
    public IterativeParallelism(){
        map = null;
    }

    /**
     * Constructor with ParallelMapper.
     *
     * Performs all operations using the given ParallelMapper.
     * @param map ParallelMapper that performs all operations
     */
    public IterativeParallelism(ParallelMapper map) {
        this.map = map;
    }

    private <T, R> R operation(int threads, List<? extends T> values,
                               Function<List<? extends T>, R> partialP,
                               Function<List<R>, R> finalP) throws InterruptedException {
        if (values.isEmpty()) {
            return null;
        }
        int usedThreads = Math.min(values.size(), threads);
        if (threads == 0) {
            return finalP.apply(List.of(partialP.apply(values)));
        }
        int threadSize = values.size() / usedThreads;
        int residual = values.size() % usedThreads;
        List<R> partialAns = new ArrayList<>(Collections.nCopies(usedThreads, null));
        int step = threadSize + 1;

        List <List<? extends T>> portions = new ArrayList<>();
        for (int i = 0; i < values.size(); i += step, residual--) {
            if (residual == 0) {
                step--;
            }
            portions.add(values.subList(i, i + step));
        }

        if (this.map != null) {
            return finalP.apply(map.map(partialP, portions));
        }

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < portions.size(); i++) {
            final int index = i;
            Thread thread = new Thread(() ->
                    partialAns.set(index, partialP.apply(portions.get(index))));
            thread.start();
            threadList.add(thread);
        }

        for (Thread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            } finally {
                t.interrupt();
            }
        }
        return finalP.apply(partialAns);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return operation(threads, values,
                (List<? extends T> l) -> l.stream().max(comparator).orElseThrow(),
                (List<T> l) -> l.stream().max(comparator).orElseThrow());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: чеоез max
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Boolean result = operation(threads, values,
                (List<? extends T> l) -> l.stream().allMatch(predicate),
                (List<Boolean> l) -> l.stream().allMatch(Boolean::valueOf));
        return result != null && result;
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: через all
        return !all(threads, values, predicate.negate());
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Integer result = operation(threads, values,
                (List<? extends T> l) -> (int) l.stream().filter(predicate).count(),
                (List<Integer> l) -> l.stream().mapToInt(Integer::intValue).sum());
        return result == null ? 0 : result;
    }
}
