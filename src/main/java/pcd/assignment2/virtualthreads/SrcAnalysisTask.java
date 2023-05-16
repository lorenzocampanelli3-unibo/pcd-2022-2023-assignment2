package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.executors.Flag;
import pcd.assignment2.executors.SourceAnalysisLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class SrcAnalysisTask implements Runnable {

    private Path src;
    private AnalysisStats stats;
    private Flag stopFlag;
    private SourceLineParser parser;

    public SrcAnalysisTask(Path src, AnalysisStats stats, Flag stopFlag) {
        this.src = src;
        this.stats = stats;
        this.stopFlag = stopFlag;
        this.parser = new SourceLineParser();
    }

    @Override
    public void run() {
        if (!stopFlag.isSet()) {
            long nLines;
            try (Stream<String> stream = Files.lines(src)) {
                nLines = stream.filter(l -> parser.parseLine(l)).count();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stats.updateFileStats(src, (int) nLines);
        }
    }
}
