package pcd.assignment2.eventloop.gui;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;
import pcd.assignment2.common.gui.InputListener;
import pcd.assignment2.eventloop.AnalysisAgent;


import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller implements InputListener {

    private Vertx vertx;
    private Flag stopFlag;
    private AnalyzerView view;


    public Controller(AnalyzerView view) {
        VertxOptions vertxOptions = new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        this.vertx = Vertx.vertx(vertxOptions);
//        this.vertx = Vertx.vertx();
        this.stopFlag = new AtomicBooleanFlag();
        this.view = view;
    }

//    public synchronized void setView(AnalyzerView view) {
//        this.view = view;
//    }

    public void started(String selectedDirPath, int maxFiles, int nBands, int maxLoc) {
        Path rootDir = Paths.get(selectedDirPath);
        String address = "analysis-data";
        this.view.updateStatus("Processing...");
        vertx.deployVerticle(new AnalysisAgent(rootDir, maxFiles, nBands, maxLoc, address, stopFlag, view));
    }

    public void stopped() {
        stopFlag.set();
    }

}
