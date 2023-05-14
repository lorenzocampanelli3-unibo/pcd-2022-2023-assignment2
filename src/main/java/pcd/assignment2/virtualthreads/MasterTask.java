package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisReport;
import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.executors.Flag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MasterTask implements Callable<AnalysisReport> {

    private Path rootDir;
    private String[] extensions;
    private AnalysisStats stats;
    private ExecutorService vtExecutor;
    private Flag stopFlag;

    public MasterTask(Path rootDir, String[] extensions, AnalysisStats stats, ExecutorService vtExecutor, Flag stopFlag) {
        this.rootDir = rootDir;
        this.extensions = extensions;
        this.stats = stats;
        this.vtExecutor = vtExecutor;
        this.stopFlag = stopFlag;
    }

    @Override
    public AnalysisReport call() throws Exception {
        if (Files.isDirectory(rootDir)) {
            long t0 = System.currentTimeMillis();
            Future<Void> rootDirAnalysisFuture = vtExecutor.submit(new SrcDiscoveryTask(rootDir, extensions, stats, vtExecutor, stopFlag ));
            rootDirAnalysisFuture.get();
            long t1 = System.currentTimeMillis();
            return new AnalysisReport(stats.getSnapshot(), (t1 - t0));
        }
        throw new IllegalArgumentException("The provided path must point to a directory.");
    }
}
