package pcd.assignment2.executors;

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
