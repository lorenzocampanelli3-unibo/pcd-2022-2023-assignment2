package pcd.assignment2.executors;

public interface AnalysisUpdateListener {
    void statsUpdated(AnalysisStatsSnapshot snapshot);
    void analysisCompleted(boolean completedSuccessfully, AnalysisReport report);
}
