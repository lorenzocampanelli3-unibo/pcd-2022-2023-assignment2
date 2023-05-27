package pcd.assignment2.virtualthreads.gui;

import pcd.assignment2.common.gui.InputListener;
import pcd.assignment2.virtualthreads.SourceAnalysisServiceVT;

import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Controller implements InputListener {

    private final Lock lock = new ReentrantLock();
//    private Flag stopFlag;
    private AnalyzerView view;
    private SourceAnalysisServiceVT sourceAnalysisService;


    public Controller(AnalyzerView view) {
//        this.stopFlag = new AtomicBooleanFlag();
        this.view = view;
        this.sourceAnalysisService = new SourceAnalysisServiceVT();
        this.sourceAnalysisService.addListener(view);
    }

//    public synchronized void setView(AnalyzerView view) {
//        this.view = view;
//    }

    public void started(String selectedDirPath, int maxFiles, int nBands, int maxLoc) {
        lock.lock();
        try {
            //        stopFlag.reset();
            sourceAnalysisService.analyseSources(Paths.get(selectedDirPath), new String[]{"java", "c", "h"}, maxFiles, nBands, maxLoc);
            view.updateStatus("Processing...");
        } finally {
            lock.unlock();
        }
    }

    public void stopped() {
        lock.lock();
        try {
            sourceAnalysisService.stopAnalysis();
        } finally {
            lock.unlock();
        }
    }

}
