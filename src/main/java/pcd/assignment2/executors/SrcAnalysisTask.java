package pcd.assignment2.executors;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

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
            try (BufferedReader reader = Files.newBufferedReader(src)) {
//                long nLines = SourceAnalysisLib.countLoC_MoreRefinedStrategy(reader);
                long nLines = SourceAnalysisLib.countLoC_SimpleStrategy(reader);
                stats.updateFileStats(src, (int) nLines);
//                stats.dumpDistribution();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
