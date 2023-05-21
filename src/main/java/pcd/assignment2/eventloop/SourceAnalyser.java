package pcd.assignment2.eventloop;

import io.vertx.core.Future;

import java.nio.file.Path;

public interface SourceAnalyser {
    Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
}
