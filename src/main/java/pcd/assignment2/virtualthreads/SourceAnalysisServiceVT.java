package pcd.assignment2.virtualthreads;

import pcd.assignment2.common.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SourceAnalysisServiceVT implements SourceAnalyserVT {

//    private static final int N_WORKERS = Runtime.getRuntime().availableProcessors() * 4;
    private static final long STATS_UPDATE_PERIOD = 50;
//    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutorService;

    private Flag stopFlag;
    private Flag isShutDown;

    private List<AnalysisUpdateListener> listeners;

    public SourceAnalysisServiceVT() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.stopFlag = new AtomicBooleanFlag();
        this.isShutDown = new AtomicBooleanFlag();
        this.listeners = new ArrayList<>();
    }

    @Override
    public Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        if (isShutDown.isSet()) {
            throw new IllegalStateException("Service is shut down.");
        }
        stopFlag.reset();
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        CompletableFuture<AnalysisReport> reportFuture = new CompletableFuture<>();
        new VTMasterAgent(rootDir, extensions, stats, stopFlag, reportFuture).start();
        return reportFuture;
    }

    @Override
    public void analyseSources(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        if (isShutDown.isSet()) {
            throw new IllegalStateException("Service is shut down.");
        }
        stopFlag.reset();
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        UpdateTask updateTask = new UpdateTask(stats, listeners);
        ScheduledFuture<?> updateTaskFuture = scheduledExecutorService.scheduleAtFixedRate(updateTask, 0, STATS_UPDATE_PERIOD, TimeUnit.MILLISECONDS);
        new Thread(() -> {
                    try {
//                       AnalysisReport report = getReport(rootDir, extensions, maxSourcesToTrack, nBands, maxLoC).get();
                        CompletableFuture<AnalysisReport> reportFuture = new CompletableFuture<>();
                        new VTMasterAgent(rootDir, new String[]{"java","c","h"}, stats, stopFlag, reportFuture).start();
                        AnalysisReport report = reportFuture.get();
                        updateTaskFuture.cancel(false);
                        boolean wasStopped = stopFlag.isSet();
                        listeners.forEach(l -> l.analysisCompleted(wasStopped, report));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }, "CompletionWaiterAgent").start();
    }

    public void addListener(AnalysisUpdateListener listener) {
        listeners.add(listener);
    }

    public void stopAnalysis() {
        this.stopFlag.set();
    }

    public boolean isAnalysisStopped() {
        return this.stopFlag.isSet();
    }

    public void shutdown() {
        this.stopFlag.set();
        this.isShutDown.set();
        try {
            this.scheduledExecutorService.shutdown();
            scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
