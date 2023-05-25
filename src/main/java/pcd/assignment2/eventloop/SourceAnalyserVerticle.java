package pcd.assignment2.eventloop;

import io.vertx.core.*;
import pcd.assignment2.common.Flag;
import pcd.assignment2.common.SourceLineParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SourceAnalyserVerticle extends AbstractVerticle {

    private Path rootDir;
    private PathMatcher matcher;
    private Notifier notifier;
//    private SourceLineParser parser;
    private Promise<Void> promise;
    private Vertx vertx;
    private Flag stopFlag;

    public SourceAnalyserVerticle(Path rootDir, PathMatcher matcher, Notifier notifier/*, SourceLineParser parser*/, Promise<Void> promise, Vertx vertx, Flag stopFlag) {
        this.rootDir = rootDir;
        this.matcher = matcher;
        this.notifier = notifier;
//        this.parser = parser;
        this.promise = promise;
        this.vertx = vertx;
        this.stopFlag = stopFlag;
    }
    @Override
    public void start() throws Exception {
        exploreForAnalysis(rootDir, matcher, notifier)
                .onSuccess(h -> {
                            promise.complete();
                        }
                )
                .onFailure(err -> {
                    promise.fail(err);
                })
                .onComplete(h -> this.vertx.undeploy(this.deploymentID()));
    }

    private Future<Void> exploreForAnalysis(Path directory, PathMatcher matcher, Notifier notifier) {
        Promise<Void> promise = Promise.promise();
        List<Future> futList = new ArrayList<>();
        var fs = vertx.fileSystem();
        var listingFut = fs.readDir(directory.toAbsolutePath().toString());
        listingFut.onSuccess(l ->
        {
            if (!stopFlag.isSet()) {
                notifier.notifyDirectoryVisitStarted(directory.toAbsolutePath().toString());
                for (String p : l) {
                    if (stopFlag.isSet()) {
                        Promise<Void> prom = Promise.promise();
                        prom.fail("Analysis interrupted");
                        log("Analysis interrupted");
                        futList.add(prom.future());
                        break;
                    }
                    Path path = Paths.get(p);
                    if (Files.isDirectory(path)) {
                        Future<Void> futExp = exploreForAnalysis(path, matcher, notifier);
                        futList.add(futExp);
                    } else if (Files.isRegularFile(path) && matcher.matches(path)) {
                        Future<Long> futNLines = asyncAnalyseFile(path);
                        futNLines.onSuccess(nLines -> notifier.notifyFileAnalysed(path.toAbsolutePath().toString(), nLines));
                        futList.add(futNLines);
                    }
                }
                CompositeFuture
                        .all(futList)
                        .onSuccess(h -> promise.complete())
                        .onFailure(err -> promise.fail(err));
            } else {
                promise.fail("Analysis interrupted");
                log("Analysis interrupted");
            }
        });
        return promise.future();
    }

    private Future<Long> asyncAnalyseFile(Path path) {
        SourceLineParser parser = new SourceLineParser();
        return vertx.executeBlocking(p -> {
            try (Stream<String> stream = Files.lines(path)) {
                if (!stopFlag.isSet()) {
                    long nLines = stream.filter(l -> parser.parseLine(l)).count();
                    p.complete(nLines);
                } else {
                    p.fail("Analysis interrupted");
                    log("Analysis interrupted");
                }
            } catch (IOException ex) {
                p.fail(ex.getMessage());
            }
        });
    }

    private void log (String msg) {
        synchronized (System.out) {
            System.out.println("[Analysis Agent] " + msg);
        }
    }
}
