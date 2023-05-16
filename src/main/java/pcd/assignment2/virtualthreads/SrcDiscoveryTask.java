package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.executors.Flag;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SrcDiscoveryTask implements Runnable {

    private final PathMatcher matcher;
    private final DirectoryStream.Filter<Path> filter;


    private Path directory;
    private String[] extensions;
    private final Queue<Thread> threadsQueue;
    private Flag stopFlag;
    private AnalysisStats stats;

    public SrcDiscoveryTask(Path directory, String[] extensions, AnalysisStats stats, Queue<Thread> threadsQueue, Flag stopFlag) {
        this.directory = directory;
        this.extensions = extensions;
        this.threadsQueue = threadsQueue;
        String syntaxAndPattern = "glob:**.{" + String.join(",", extensions) + "}";
        this.matcher =  FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        this.filter = path -> Files.isDirectory(path) || this.matcher.matches(path);

        this.stats = stats;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        if (!stopFlag.isSet()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
//                Log.log("Exploring directory: " + directory.toString());
                Iterator<Path> iterator = stream.iterator();
                while (!stopFlag.isSet() && iterator.hasNext()) {
                    Path entry = iterator.next();
                    Thread task;
                    if (Files.isDirectory(entry)) {
                        task = Thread.startVirtualThread(new SrcDiscoveryTask(entry, extensions, stats, threadsQueue, stopFlag));
                    } else {
                        task = Thread.startVirtualThread(new SrcAnalysisTask(entry, stats, stopFlag));
                    }
                    threadsQueue.add(task);
                }
                stats.updateDirStats();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    private void explore(Path directory) {
//        if (!stopFlag.isSet()) {
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
////                Log.log("Exploring directory: " + directory.toString());
//                Iterator<Path> iterator = stream.iterator();
//                while (!stopFlag.isSet() && iterator.hasNext()) {
//                    Path entry = iterator.next();
//                    if (Files.isDirectory(entry)) {
//                        explore(entry);
//                    } else {
//                        Future<Void> fut = executor.submit(new SrcAnalysisTask(entry, stats, stopFlag));
//                        futuresList.add(fut);
//                    }
//                }
//                stats.updateDirStats();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
