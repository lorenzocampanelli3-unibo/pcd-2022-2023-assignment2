package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisStats;
import pcd.assignment2.executors.Flag;
import pcd.assignment2.executors.SourceAnalysisLib;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class SrcAnalysisTask implements Callable<Void> {

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
    public Void call() throws Exception {
        if (!stopFlag.isSet()) {
            long nLines;
            try(Stream<String> stream = Files.lines(src)) {
                nLines = stream.filter(l -> parser.parseLine(l)).count();
            }
            stats.updateFileStats(src, (int) nLines);
//            try (BufferedReader reader = Files.newBufferedReader(src)) {
//                long nLines = SourceAnalysisLib.countLoC_MoreRefinedStrategy(reader);
////                long nLines = SourceAnalysisLib.countLoC_SimpleStrategy(reader);
//                stats.updateFileStats(src, (int) nLines);
////                stats.dumpDistribution();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
        }
        return null;
    }
}
