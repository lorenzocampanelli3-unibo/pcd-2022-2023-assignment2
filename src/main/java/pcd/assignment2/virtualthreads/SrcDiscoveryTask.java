package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisReport;
import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.executors.Flag;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SrcDiscoveryTask implements Callable<Void> {

    private final PathMatcher matcher;
    private final DirectoryStream.Filter<Path> filter;


    private Path directory;
    private String[] extensions;
    private ExecutorService executor;
    private Flag stopFlag;
    private AnalysisStats stats;
    private List<Future<Void>> futuresList;

    public SrcDiscoveryTask(Path directory, String[] extensions, AnalysisStats stats, ExecutorService executor, Flag stopFlag) {
        this.directory = directory;
        this.extensions = extensions;
        String syntaxAndPattern = "glob:**.{" + String.join(",", extensions) + "}";
        this.matcher =  FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        this.filter = path -> Files.isDirectory(path) || this.matcher.matches(path);

        this.stats = stats;
        this.executor = executor;
        this.stopFlag = stopFlag;
        this.futuresList = new LinkedList<>();
    }

    @Override
    public Void call() throws Exception {
        if (!stopFlag.isSet()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
//                Log.log("Exploring directory: " + directory.toString());
                Iterator<Path> iterator = stream.iterator();
                while (!stopFlag.isSet() && iterator.hasNext()) {
                    Path entry = iterator.next();
                    Future<Void> fut;
                    if (Files.isDirectory(entry)) {
                        fut = executor.submit(new SrcDiscoveryTask(entry, extensions, stats, executor, stopFlag));
                    } else {
                        fut = executor.submit(new SrcAnalysisTask(entry, stats, stopFlag));
                    }
                    futuresList.add(fut);
                }
                stats.updateDirStats();
                futuresList.forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
