package pcd.assignment2.eventloop;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SourceAnalyserLib implements SourceAnalyser {

    private Vertx vertx;
    private Flag stopFlag;

    private SourceLineParser parser;

    public SourceAnalyserLib(Vertx vertx, Flag stopFlag) {
        this.vertx = vertx;
        this.stopFlag = stopFlag;
        this.parser = new SourceLineParser();
    }

    @Override
    public Future<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        Promise<AnalysisReport> promise = Promise.promise();
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{" + String.join(",", extensions) + "}");
        long t0 = System.currentTimeMillis();
        explore(rootDir, matcher, stats)
                .onSuccess(h -> {
                        long t1 = System.currentTimeMillis();
                        promise.complete(new AnalysisReport(stats.getSnapshot(), (t1-t0)));
                    }
                );
        return promise.future();
    }

    private Future<Void> explore(Path directory, PathMatcher matcher, AnalysisStats stats) {
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
                    Future<Void> futExp = explore(path, matcher, stats);
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
