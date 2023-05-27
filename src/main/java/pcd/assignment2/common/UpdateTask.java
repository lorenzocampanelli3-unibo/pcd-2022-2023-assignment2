package pcd.assignment2.common;

import pcd.assignment2.common.AnalysisStats;
import pcd.assignment2.common.AnalysisStatsSnapshot;
import pcd.assignment2.common.AnalysisUpdateListener;

import java.util.List;

public class UpdateTask implements Runnable{

    private AnalysisStats stats;
    private List<AnalysisUpdateListener> listeners;

    public UpdateTask(AnalysisStats stats, List<AnalysisUpdateListener> listeners) {
        this.listeners = listeners;
        this.stats = stats;
    }

    @Override
    public void run() {
        AnalysisStatsSnapshot snapshot = this.stats.getSnapshot();
        listeners.forEach(l -> l.statsUpdated(snapshot));
    }
}
