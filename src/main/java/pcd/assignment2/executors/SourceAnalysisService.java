package pcd.assignment2.executors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SourceAnalysisService implements SourceAnalyser {

//    private static final int N_WORKERS = Runtime.getRuntime().availableProcessors() * 4;
    private static final long STATS_UPDATE_PERIOD = 50;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutorService;

    private Flag stopFlag;
    private Flag isShutDown;

    private List<AnalysisUpdateListener> listeners;

    public SourceAnalysisService(int nWorkers) {
        this.executor = Executors.newFixedThreadPool(nWorkers);
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
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        return executor.submit(new SrcDiscoveryTask(rootDir, extensions, stats, executor, stopFlag));
    }

    @Override
    public void analyseSources(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        if (isShutDown.isSet()) {
            throw new IllegalStateException("Service is shut down.");
        }
        stopFlag.reset();
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
//        Path rootDirPath = Paths.get(rootDir);
        SrcDiscoveryTask srcDiscoveryTask = new SrcDiscoveryTask(rootDir, extensions, stats, executor, stopFlag);
        UpdateTask updateTask = new UpdateTask(stats, listeners);
        ScheduledFuture<?> updateTaskFuture = scheduledExecutorService.scheduleAtFixedRate(updateTask, 0, STATS_UPDATE_PERIOD, TimeUnit.MILLISECONDS);
        Future<AnalysisReport> analysisFuture = executor.submit(new SrcDiscoveryTask(rootDir, extensions, stats, executor, stopFlag));
        new Thread(() -> {
                    try {
                        AnalysisReport report = analysisFuture.get();
                        updateTaskFuture.cancel(false);
                        boolean completedSuccessfully = !stopFlag.isSet();
                        listeners.forEach(l -> l.analysisCompleted(completedSuccessfully, report));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }, "AnalysisCompletionWaiter").start();
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
            this.executor.shutdown();
            this.scheduledExecutorService.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            scheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
