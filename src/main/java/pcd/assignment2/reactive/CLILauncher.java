package pcd.assignment2.reactive;

import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CLILauncher {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Missing command line arguments.");
            System.exit(1);
        }
        String D = args[0];
        int MAXL = Integer.parseInt(args[1]);
        int NI = Integer.parseInt(args[2]);
        int topN = Integer.parseInt(args[3]);
        Path rootDir = Paths.get(D);
        if (!Files.exists(rootDir)) {
            System.err.println("The specified directory does not exist.");
            System.exit(1);
        }
        System.out.println("Starting with params:\n" +
                "rootDir = " + D + ", NI: " + NI + ", MAXL: " + MAXL + ", topN: " + topN);
        Flag stopFlag = new AtomicBooleanFlag();
        SourceAnalyserRxLib lib = new SourceAnalyserRxLib(stopFlag);
        lib.getReport(rootDir, new String[]{"java", "c", "h"}, topN, NI, MAXL)
                .subscribe(r -> {
                    r.dumpTopFilesRanking();
                    r.dumpDistribution();
                    System.out.println("# dirs analysed: " + r.getSnapshot().getNumDirectoriesProcessed());
                    System.out.println("# files analysed: " + r.getSnapshot().getNumSourcesProcessed());
                    System.out.println("Elapsed time: " + r.getElapsedTime() + "ms.");
                });
    }
}
