package pcd.assignment2.virtualthreads;

import pcd.assignment2.common.AnalysisReport;

import java.nio.file.Path;
import java.util.concurrent.Future;

public interface SourceAnalyserVT {
    /**
     *
     * @param rootDir the starting directory
     * This implementation with Virtual Threads returns void because it simply "enqueues" the job.
     * Then you can get the {@link AnalysisReport} calling the {@link AnalysisReport#awaitAndGetReport} method
     */
    Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
    void analyseSources(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
}
