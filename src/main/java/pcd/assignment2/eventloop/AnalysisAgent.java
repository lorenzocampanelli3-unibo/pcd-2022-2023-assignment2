package pcd.assignment2.eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.Flag;
import pcd.assignment2.eventloop.gui.AnalyzerView;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AnalysisAgent extends AbstractVerticle {

    private Path rootDir;
    private AnalysisStats stats;
    private String address;
    private Flag stopFlag;
    private AnalyzerView view;

    public AnalysisAgent(Path rootDir, int maxSourcesToTrack, int nBands, int maxLoC, String address, Flag stopFlag, AnalyzerView view) {
        this.stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        this.rootDir = rootDir;
        this.address = address;
        this.stopFlag = stopFlag;
        this.view = view;
    }

    @Override
    public void start() throws Exception {

        this.getVertx().eventBus().consumer(address, msg -> {
            JsonObject json = (JsonObject) msg.body();

            String event = json.getString("event");

            switch (event) {
                case Notifier.NEW_DIRECTORY_VISIT_STARTED -> stats.updateDirStats();
                case Notifier.NEW_FILE_ANALYSED -> {
                    String pathString = json.getString("file_path");
                    long nLines = json.getLong("nloc");
                    stats.updateFileStats(Paths.get(pathString), (int) nLines);
                }
            }
        });

        long timerID = vertx.setPeriodic(50, id -> {
            view.statsUpdated(stats.getSnapshot());
        });

        stopFlag.reset();

        long t0 = System.currentTimeMillis();
        new SourceAnalyserLib(vertx, stopFlag)
                .analyseSources(rootDir, new String[]{"java", "c", "h"}, address)
                .onSuccess(h -> {
                    long t1 = System.currentTimeMillis();
                    vertx.cancelTimer(timerID);
                    this.view.analysisCompleted(false, new AnalysisReport(stats.getSnapshot(), (t1-t0)));
                    vertx.undeploy(this.deploymentID());
                })
                .onFailure(err -> {
                    long t1 = System.currentTimeMillis();
                    vertx.cancelTimer(timerID);
                    this.view.analysisCompleted(true, new AnalysisReport(stats.getSnapshot(), (t1-t0)));
                    vertx.undeploy(this.deploymentID());
                });
    }
}
