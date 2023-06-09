package pcd.assignment2.executors.gui;

import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.AnalysisStatsSnapshot;
import pcd.assignment2.common.AnalysisUpdateListener;
import pcd.assignment2.common.gui.AnalyzerUI;
import pcd.assignment2.common.gui.InputListener;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class AnalyzerView implements AnalysisUpdateListener {

    private final Lock lock = new ReentrantLock();
    private AnalyzerUI ui;

    public AnalyzerView(String defRootDir, int defMaxLoC, int defNBands, int defNTopFiles) {
        this.ui = new AnalyzerUI("Executors", defRootDir, defMaxLoC, defNBands, defNTopFiles, 4, false);
    }

    public void addListener(InputListener listener) {
        lock.lock();
        try {
            ui.addListener(listener);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void statsUpdated(AnalysisStatsSnapshot snapshot) {
        lock.lock();
        try {
            ui.updateStats(snapshot);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void analysisCompleted(boolean wasStopped, AnalysisReport report) {
        lock.lock();
        try {
            AnalysisStatsSnapshot snapshot = report.getSnapshot();
            ui.updateStats(snapshot);
            if (wasStopped) {
                ui.updateStatus("Analysis interrupted by the user.");
            } else {
                ui.updateStatus("Analysis completed.");
            }
            ui.updateStatus("Analysed " + snapshot.getNumSourcesProcessed() + " source files in " + snapshot.getNumDirectoriesProcessed() + " directories");
            ui.updateStatus("Elapsed time: " + report.getElapsedTime() + " ms.");
            ui.reset();
        } finally {
            lock.unlock();
        }
    }

    public void updateStatus(String text) {
        lock.lock();
        try {
            ui.updateStatus(text);
        } finally {
            lock.unlock();
        }

    }

    public void display() {
        lock.lock();
        try {
            ui.display();
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        lock.lock();
        try {
            ui.reset();
        } finally {
            lock.unlock();
        }
    }
}
