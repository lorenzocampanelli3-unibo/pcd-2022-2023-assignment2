package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisReport;

import java.nio.file.Path;
import java.util.concurrent.Future;

public interface SourceAnalyser {
    /**
     *
     * @param rootDir the starting directory
     * This implementation with Virtual Threads returns void because it simply "enqueues" the job.
     * Then you can get the {@link AnalysisReport} calling the {@link AnalysisReport#awaitAndGetReport} method
     */
    void getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
    void analyseSources(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
}
