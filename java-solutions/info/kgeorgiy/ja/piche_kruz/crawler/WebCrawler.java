package info.kgeorgiy.ja.piche_kruz.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    public static void main(String[] args) {
        // :NOTE: WebCrawler url [depth [downloads [extractors [perHost]]]]
        // WebCrawler example.com 10
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("Invalid argument");
            return;
        }

        final int depth, downloaders, extractors, perHost;
        final String url = Objects.requireNonNull(args[0]);
        final double timeScale = 100;
        depth = getArgs(args, 1,1);
        downloaders = getArgs(args, 2,1);
        extractors = getArgs(args, 3,1);
        perHost = getArgs(args, 4,4);
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(timeScale), downloaders, extractors, perHost)) {
            Result result = crawler.download(url, depth);
            System.out.println("Downloaded");
            for (String it : result.getDownloaded()) {
                System.out.println(it);
            }
            if (!result.getErrors().isEmpty()) {
                System.out.println("Downloaded pages with errors:");
                for (Map.Entry<String, IOException> s : result.getErrors().entrySet()) {
                    System.out.println(s.getKey() + " " + s.getValue().getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot create new CachingDownloader");
        }

    }

    private static class DfsSearcher {
        final Set<String> downloadedLinks = ConcurrentHashMap.newKeySet();
        List<String> currentLayer = new ArrayList<>();
        final Set<String> extractedLinks = ConcurrentHashMap.newKeySet();
        final Set<String> newLayer = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Phaser phaser = new Phaser(1);

        private void addToNextLayer(List<String> s) {
            newLayer.addAll(s);
        }

        public void arriveAndAwaitAdvance() {
            phaser.arriveAndAwaitAdvance();
        }

        public void createNextLayer() {
            currentLayer = List.copyOf(newLayer);
            newLayer.clear();
        }

        public boolean addToAns(String url) {
            return extractedLinks.add(url);
        }

        public void register() {
            phaser.register();
        }

        public void putError(String url, IOException e) {
            errors.put(url, e);
        }

        public void arriveAndDeregister() {
            phaser.arriveAndDeregister();
        }

        public void addToDownloaded(String url) {
            downloadedLinks.add(url);
        }

        public boolean contains(String link) {
            return extractedLinks.contains(link);
        }
    }


    @Override
    public Result download(String url, int depth) {
        // :NOTE: extract to class
        DfsSearcher searcher = new DfsSearcher();
        searcher.currentLayer.add(url);
        for(int i = depth; i > 0; i--) {
            for (String s : searcher.currentLayer) {
                downloadPage(s, depth, searcher);
            }
            searcher.arriveAndAwaitAdvance();
            searcher.createNextLayer();
        }
        return new Result(new ArrayList<>(searcher.downloadedLinks), searcher.errors);
    }

    private void downloadPage(String url, int depth, DfsSearcher searcher) {
        searcher.register();
        downloaders.submit(() -> {
            try {
                if (searcher.addToAns(url)) {
                    Document document = downloader.download(url);
                    searcher.addToDownloaded(url);
                    extractTask(url, depth, searcher, document);
                }
            } catch (IOException e) {
                searcher.putError(url, e);
            } finally {
                searcher.arriveAndDeregister();
            }
        });
    }

    private void extractTask(String url, int depth, DfsSearcher searcher, Document document) {
        if (depth > 1) {
            searcher.register();
            extractors.submit(() -> {
                try {
                    List<String> temp = new ArrayList<>();
                    for (String link : document.extractLinks()) {
                        if (!searcher.contains(link)) {
                            temp.add(link);
                        }
                    }
                    searcher.addToNextLayer(temp);
                } catch (IOException e) {
                    searcher.putError(url, e);
                } finally {
                    searcher.arriveAndDeregister();
                }
            });
        }
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    private static int getArgs(final String[] args, int i, int defaultValue) {
        if (args.length < i) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(args[i]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error: illegal arguments");
        }
    }
}
