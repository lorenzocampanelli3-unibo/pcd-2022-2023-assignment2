package pcd.assignment2.virtualthreads;

import pcd.assignment2.executors.AnalysisReport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CLILauncher {
//    private static final int MAX_NUM_WORKERS = Runtime.getRuntime().availableProcessors() * 4;

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
        SourceAnalysisService sourceAnalysisService = new SourceAnalysisService();
        Future<AnalysisReport> reportFuture = sourceAnalysisService.getReport(rootDir, new String[]{"java", "c", "h"},  topN, NI, MAXL);
        try {
            AnalysisReport report = reportFuture.get();
            report.dumpTopFilesRanking();
            report.dumpDistribution();
            System.out.println("Elapsed time: " + report.getElapsedTime() + " ms.");
            sourceAnalysisService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
