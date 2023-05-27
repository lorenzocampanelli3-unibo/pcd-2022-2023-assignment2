package pcd.assignment2.eventloop;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.AnalysisStatsNoLock;
import pcd.assignment2.common.Flag;
import pcd.assignment2.common.SourceLineParser;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SourceAnalyserEvLoopLib implements SourceAnalyserEvLoop {

    private Vertx vertx;
    private Flag stopFlag;

//    private SourceLineParser parser;

    public SourceAnalyserEvLoopLib(Vertx vertx, Flag stopFlag) {
        this.vertx = vertx;
        this.stopFlag = stopFlag;
//        this.parser = new SourceLineParser();
    }

    @Override
    public Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        Promise<AnalysisReport> promise = Promise.promise();
        AnalysisStatsNoLock stats = new AnalysisStatsNoLock(rootDir, maxSourcesToTrack, nBands, maxLoC);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{" + String.join(",", extensions) + "}");
        long t0 = System.currentTimeMillis();
        exploreForReport(rootDir, matcher, stats)
                .onSuccess(h -> {
                        long t1 = System.currentTimeMillis();
                        promise.complete(new AnalysisReport(stats.getSnapshot(), (t1-t0)));
                    }
                );
        return promise.future();
    }

    @Override
    public Future<Void> analyseSources(Path rootDir, String[] extensions, String address) {
        Promise<Void> promise = Promise.promise();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{" + String.join(",", extensions) + "}");
        Notifier notifier = new Notifier(vertx, address);
        vertx.deployVerticle(new SourceAnalyserVerticle(rootDir, matcher, notifier/*, parser*/, promise, vertx, stopFlag));
        return promise.future();
    }


    private Future<Void> exploreForReport(Path directory, PathMatcher matcher, AnalysisStatsNoLock stats) {
        Promise<Void> promise = Promise.promise();
        List<Future> futList = new ArrayList<>();
        var fs = vertx.fileSystem();
        var listingFut = fs.readDir(directory.toAbsolutePath().toString());
        listingFut.onSuccess(l ->
        {
            stats.updateDirStats();
            for (String p : l) {
                Path path = Paths.get(p);
                if (Files.isDirectory(path)) {
                    Future<Void> futExp = exploreForReport(path, matcher, stats);
                    futList.add(futExp);
                } else if (Files.isRegularFile(path) && matcher.matches(path)) {
                    Future<Long> futNLines = asyncGetFileNumLines(path);
                    futNLines.onSuccess(nLines -> stats.updateFileStats(path, Math.toIntExact(nLines)));
                    futList.add(futNLines);
                }
            }
            CompositeFuture
                    .all(futList)
                    .onSuccess(h -> promise.complete());
        });
        return promise.future();
    }



    private Future<Long> asyncGetFileNumLines(Path path) {
        SourceLineParser parser = new SourceLineParser();
        return vertx.executeBlocking(p -> {
            try (Stream<String> stream = Files.lines(path)) {
                long nLines = stream.filter(l -> parser.parseLine(l)).count();
                p.complete(nLines);
            } catch (IOException ex) {
                p.fail(ex.getMessage());
            }
        });
    }

}
