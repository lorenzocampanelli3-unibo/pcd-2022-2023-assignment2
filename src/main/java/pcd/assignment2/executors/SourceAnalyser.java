package pcd.assignment2.executors;

import java.nio.file.Path;
import java.util.concurrent.Future;

public interface SourceAnalyser {
    /**
     *
     * @param rootDir the starting directory
     * @return a Future what will contain an {@link AnalysisReport} when the exploration finishes.
     */
    Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
    void analyseSources(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
}
