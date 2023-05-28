package pcd.assignment2.common;

public interface AnalysisUpdateListener {
    void statsUpdated(AnalysisStatsSnapshot snapshot);
    void analysisCompleted(boolean wasStopped, AnalysisReport report);
}
