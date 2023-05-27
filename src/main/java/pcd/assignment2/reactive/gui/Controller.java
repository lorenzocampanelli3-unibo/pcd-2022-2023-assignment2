package pcd.assignment2.reactive.gui;

import io.vertx.core.Vertx;
import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;
import pcd.assignment2.common.gui.InputListener;
import pcd.assignment2.reactive.AnalysisAgent;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller implements InputListener {

    private Vertx vertx;
    private Flag stopFlag;
    private AnalyzerView view;


    public Controller(AnalyzerView view) {
        this.vertx = Vertx.vertx();
        this.stopFlag = new AtomicBooleanFlag();
        this.view = view;
    }

//    public synchronized void setView(AnalyzerView view) {
//        this.view = view;
//    }

    public void started(String selectedDirPath, int maxFiles, int nBands, int maxLoc) {
        Path rootDir = Paths.get(selectedDirPath);
        this.view.updateStatus("Processing...");
        vertx.deployVerticle(new AnalysisAgent(rootDir, maxFiles, nBands, maxLoc, stopFlag, view));
    }

    public void stopped() {
        stopFlag.set();
    }

}
