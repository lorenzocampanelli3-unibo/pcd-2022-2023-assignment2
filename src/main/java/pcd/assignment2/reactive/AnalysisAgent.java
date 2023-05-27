package pcd.assignment2.reactive;

import io.vertx.core.AbstractVerticle;
import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.AnalysisStats;
import pcd.assignment2.common.Flag;
import pcd.assignment2.reactive.gui.AnalyzerView;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AnalysisAgent extends AbstractVerticle {

    private Path rootDir;
    private AnalysisStats stats;

    private Flag stopFlag;
    private AnalyzerView view;

    public AnalysisAgent(Path rootDir, int maxSourcesToTrack, int nBands, int maxLoC, Flag stopFlag, AnalyzerView view) {
        this.rootDir = rootDir;
        this.stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        this.stopFlag = stopFlag;
        this.view = view;
    }

    @Override
    public void start() throws Exception {
        long timerID = vertx.setPeriodic(10, id -> {
            view.statsUpdated(stats.getSnapshot());
        });
        stopFlag.reset();

        long t0 = System.currentTimeMillis();
        new SourceAnalyserRxLib(stopFlag)
                .analyseSources(rootDir, new String[]{"java", "c", "h"})
                .subscribe(item -> {
//                    logThread("New event");
                    String event = item.getString("event");
                    switch (event) {
                        case Notifications.NEW_DIRECTORY_VISIT_STARTED -> stats.updateDirStats();
                        case Notifications.NEW_FILE_ANALYSED ->  {
                            String src = item.getString("file_path");
                            long nLines = item.getLong("nloc");
                            stats.updateFileStats(Paths.get(src), (int) nLines);
                        }
                    }
                }, throwable -> {
                    log("Error occured: " + throwable.getMessage());
                }, () -> {
                    long t1 = System.currentTimeMillis();
                    vertx.cancelTimer(timerID);
                    if (!stopFlag.isSet()) {
                        log("Analysis complete.");
                        this.view.analysisCompleted(false, new AnalysisReport(stats.getSnapshot(), (t1 - t0)));
                    } else {
                        log("Analysis interrupted by the user.");
                        this.view.analysisCompleted(true, new AnalysisReport(stats.getSnapshot(), (t1 - t0)));
                    }
                    vertx.undeploy(this.deploymentID());
                });
    }

    private void log(String msg) {
        System.out.println("[Analysis Agent] " + msg);
    }

    private void logThread(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
