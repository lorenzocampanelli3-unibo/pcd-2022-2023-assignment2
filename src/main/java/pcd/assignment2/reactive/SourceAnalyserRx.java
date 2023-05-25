package pcd.assignment2.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pcd.assignment2.common.AnalysisReport;

import java.nio.file.Path;

public interface SourceAnalyserRx {
    Observable<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC);
    Observable<JsonObject> analyseSources(Path rootDir, String[] extensions);

}
