package pcd.assignment2.eventloop.gui;

import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.AnalysisStatsSnapshot;
import pcd.assignment2.common.AnalysisUpdateListener;
import pcd.assignment2.common.gui.AnalyzerUI;
import pcd.assignment2.common.gui.InputListener;


public class AnalyzerView implements AnalysisUpdateListener {

    private AnalyzerUI ui;

    public AnalyzerView(String defRootDir, int defMaxLoC, int defNBands, int defNTopFiles) {
        this.ui = new AnalyzerUI("Event Loop", defRootDir, defMaxLoC, defNBands, defNTopFiles, 4, false);
    }

    public void addListener(InputListener listener) {
        ui.addListener(listener);
    }

    @Override
    public void statsUpdated(AnalysisStatsSnapshot snapshot) {
        ui.updateStats(snapshot);
    }

    @Override
    public void analysisCompleted(boolean wasStopped, AnalysisReport report) {
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
    }

    public void updateStatus(String text) {
        ui.updateStatus(text);
    }

    public void display() {
        ui.display();
    }

    public void reset() {
        ui.reset();
    }
}
