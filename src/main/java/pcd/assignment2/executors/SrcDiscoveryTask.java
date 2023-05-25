package pcd.assignment2.executors;

import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.Flag;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SrcDiscoveryTask implements Callable<AnalysisReport> {

    private final Path rootDir;
    private final PathMatcher matcher;
    private final DirectoryStream.Filter<Path> filter;


    private ExecutorService executor;
    private Flag stopFlag;
    private AnalysisStats stats;
    private List<Future<Void>> futuresList;

    public SrcDiscoveryTask(Path rootDir, String[] extensions, AnalysisStats stats, ExecutorService executor, Flag stopFlag) {
        this.rootDir = rootDir;
        String syntaxAndPattern = "glob:**.{" + String.join(",", extensions) + "}";
        this.matcher =  FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        this.filter = path -> Files.isDirectory(path) || this.matcher.matches(path);

        this.stats = stats;
        this.executor = executor;
        this.stopFlag = stopFlag;
        this.futuresList = new LinkedList<>();
    }

    @Override
    public AnalysisReport call() throws Exception {
        long t1 = System.currentTimeMillis();
//        Log.log("Started directory listing.");
        if (Files.isDirectory(rootDir)) {
            explore(rootDir);
            futuresList.forEach(f -> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            throw new IllegalArgumentException("The provided path must point to a directory.");
        }
        long t2 = System.currentTimeMillis();
        return new AnalysisReport(stats.getSnapshot(), (t2-t1));
    }

    private void explore(Path directory) {
        if (!stopFlag.isSet()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
//                Log.log("Exploring directory: " + directory.toString());
                Iterator<Path> iterator = stream.iterator();
                while (!stopFlag.isSet() && iterator.hasNext()) {
                    Path entry = iterator.next();
                    if (Files.isDirectory(entry)) {
                        explore(entry);
                    } else {
                        Future<Void> fut = executor.submit(new SrcAnalysisTask(entry, stats, stopFlag));
                        futuresList.add(fut);
                    }
                }
                stats.updateDirStats();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
