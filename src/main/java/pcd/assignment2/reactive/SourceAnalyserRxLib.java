package pcd.assignment2.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import pcd.assignment2.common.*;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class SourceAnalyserRxLib implements SourceAnalyserRx {

    private Flag stopFlag;

    public SourceAnalyserRxLib(Flag stopFlag) {
        this.stopFlag = stopFlag;
    }

    @Override
    public Observable<AnalysisReport> getReport(Path rootDir, String[] extensions, int maxSourcesToTrack, int nBands, int maxLoC) {
        AnalysisStats stats = new AnalysisStats(rootDir, maxSourcesToTrack, nBands, maxLoC);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{" + String.join(",", extensions) + "}");
        long t0 = System.currentTimeMillis();
        Observable.just(rootDir).mergeWith(collectDirectories(rootDir))
                .subscribeOn(Schedulers.io())
                .doOnNext(d -> {/*log("New folder");*/ stats.updateDirStats();})
                .flatMap(d -> {/*log("flatMap");*/ return matchingFilesInDirectory(d, matcher);})
                .map(f -> {/*log("countLines");*/ return countLines(f);})
                .blockingSubscribe(pair -> {/*log("blockingSubscribe handler");*/ stats.updateFileStats(pair.getX(), Math.toIntExact(pair.getY()));});
//                .doOnNext(pair -> {/*log("blockingSubscribe handler");*/ stats.updateFileStats(pair.getX(), Math.toIntExact(pair.getY()));})
//                .blockingSubscribe();
        long t1 = System.currentTimeMillis();
        return Observable.just(new AnalysisReport(stats.getSnapshot(), (t1-t0)));
    }

    @Override
    public Observable<JsonObject> analyseSources(Path rootDir, String[] extensions) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{" + String.join(",", extensions) + "}");
        return Observable.just(rootDir).mergeWith(collectDirectories(rootDir))
                .flatMap(d -> {/*log("Dir: " + d);*/ return analyseDirectory(d, matcher);})
                .takeUntil(j -> {return stopFlag.isSet();});
    }

    private Observable<JsonObject> analyseDirectory(Path directory, PathMatcher matcher) {
//        log("Analysing directory: " + directory);
        Observable<JsonObject> dirAnalysisStartedNotification = Observable.just(
                                    Notifications.newDirectoryVisitStarted(
                                            directory.toAbsolutePath().toString())
                                    );
        Observable<Path> allFiles = Observable.create(emitter -> {
//            log("All files elem generation");
            try(Stream<Path> stream = Files.list(directory)) {
                stream.forEach(p -> {/*log("Elem: " + p);*/ emitter.onNext(p);});
            } catch (ClosedByInterruptException ignored) {}
            emitter.onComplete();
        });
        Observable<Path> filesToBeExamined =
                allFiles
                        .filter(f -> Files.isRegularFile(f) && matcher.matches(f));
        Observable<JsonObject> jsonStream = filesToBeExamined.map(f -> {
//                                                                        log("File: " + f);
                                                                          long nLines = countLinesForAnalysis(f);
//                                                                          log("# lines: " + nLines);
                                                                          var notif =  Notifications.newFileAnalysed(
                                                                                  f.toAbsolutePath().toString(), nLines);
//                                                                          log("Notif: " + notif.toString());
                                                                          return  notif;
                                                                        });
        return dirAnalysisStartedNotification.mergeWith(jsonStream).subscribeOn(Schedulers.io());
//        return dirAnalysisStartedNotification.mergeWith(jsonStream).subscribeOn(Schedulers.computation());
    }

    private Observable<Path> collectDirectories(Path directory) {
        Observable<Path> allFiles = Observable.create(emitter -> {
            try(Stream<Path> stream = Files.list(directory)) {
                stream.forEach(p -> emitter.onNext(p));
            } catch (ClosedByInterruptException ignored) {}
            emitter.onComplete();
        });

        Observable<Path> directories =
                allFiles
                .filter(f -> {/*log("collecting dir...");*/ return Files.isDirectory(f);});
        Observable<Path> allSubDirs =
                directories
                        .flatMap(d -> { return collectDirectories(d.toAbsolutePath());});
        return directories.mergeWith(allSubDirs).subscribeOn(Schedulers.io()); //TODO: Attenzione
//        return directories.mergeWith(allSubDirs); //TODO: Attenzione
    }

    private Pair<Path, Long> countLines(Path file) {
//        log("In countLines");
        long nLines = 0;
        SourceLineParser parser = new SourceLineParser();
        try (Stream<String> stream = Files.lines(file)) {
            nLines = stream.filter(l -> parser.parseLine(l)).count();
        } catch (ClosedByInterruptException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Pair<>(file, nLines);
    }

    private long countLinesForAnalysis(Path file) {
//        log("In countLines");
        SourceLineParser parser = new SourceLineParser();
        try (Stream<String> stream = Files.lines(file)) {
            return stream.filter(l -> parser.parseLine(l)).count();
        } catch (ClosedByInterruptException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private Observable<Path> matchingFilesInDirectory(Path directory, PathMatcher matcher) {
//        log("In matchingFilesIn directory");
        Observable<Path> allFiles = Observable.create(emitter -> {
            try(Stream<Path> stream = Files.list(directory)) {
                stream.forEach(p -> emitter.onNext(p));
            } catch (ClosedByInterruptException ignored) {}
            emitter.onComplete();
        });

        Observable<Path> filesToBeExamined = allFiles.filter(f -> {/*log("matchingFilesInDirectory");*/ return Files.isRegularFile(f) && matcher.matches(f);});
        return filesToBeExamined.subscribeOn(Schedulers.io());
    }

    static private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
        }
    }
}
