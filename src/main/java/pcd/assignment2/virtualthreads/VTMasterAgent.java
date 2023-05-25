package pcd.assignment2.virtualthreads;

import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.common.Flag;

import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VTMasterAgent extends Thread{

    private Path rootPath;
    private String[] extensions;
    private AnalysisStats stats;
    private Queue<Thread> threadsQueue;
    private CompletableFuture<AnalysisReport> future;
    private Flag stopFlag;

    public VTMasterAgent(Path rootPath, String[] extensions, AnalysisStats stats, Flag stopFlag, CompletableFuture<AnalysisReport> future) {
        super("VT Master Agent");
        this.rootPath = rootPath;
        this.extensions = extensions;
        this.stats = stats;
        this.stopFlag = stopFlag;
        this.threadsQueue = new ConcurrentLinkedQueue<>();
        this.future = future;
    }

    @Override
    public void run() {
        SrcDiscoveryTask rootTask = new SrcDiscoveryTask(rootPath, extensions, stats, threadsQueue, stopFlag);
        Thread rootVT = Thread.ofVirtual().unstarted(rootTask);
        threadsQueue.add(rootVT);
        long t0 = System.currentTimeMillis();
        rootVT.start();
        Thread workerVT;
        while ((workerVT = threadsQueue.poll()) != null) {
            try {
                workerVT.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long t1 = System.currentTimeMillis();
        future.complete(new AnalysisReport(stats.getSnapshot(), (t1 - t0)));
    }
}
