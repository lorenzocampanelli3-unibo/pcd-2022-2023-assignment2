package pcd.assignment2.eventloop;

import io.vertx.core.Future;
import pcd.assignment2.common.AnalysisReport;

import java.nio.file.Path;

public interface SourceAnalyserEvLoop {
    Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
    Future<Void> analyseSources(Path rootDir, String[] extensions, String address);

}
