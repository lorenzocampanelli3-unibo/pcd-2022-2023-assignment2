package pcd.assignment2.executors;

import pcd.assignment2.common.AnalysisStats;
import pcd.assignment2.common.Flag;
import pcd.assignment2.common.SourceLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class SrcAnalysisTask implements Callable<Void> {

    private Path src;
    private AnalysisStats stats;
    private Flag stopFlag;
    public SrcAnalysisTask(Path src, AnalysisStats stats, Flag stopFlag) {
        this.src = src;
        this.stats = stats;
        this.stopFlag = stopFlag;
    }

    @Override
    public Void call() throws Exception {
        if (!stopFlag.isSet()) {
//            try (BufferedReader reader = Files.newBufferedReader(src)) {
////                long nLines = SourceAnalysisLib.countLoC_MoreRefinedStrategy(reader);
//                long nLines = SourceAnalysisLib.countLoC_SimpleStrategy(reader);
//                stats.updateFileStats(src, (int) nLines);
////                stats.dumpDistribution();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            SourceLineParser parser = new SourceLineParser();
            try (Stream<String> stream = Files.lines(src)) {
                long nLines = stream.filter(l -> parser.parseLine(l)).count();
                stats.updateFileStats(src, (int) nLines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
