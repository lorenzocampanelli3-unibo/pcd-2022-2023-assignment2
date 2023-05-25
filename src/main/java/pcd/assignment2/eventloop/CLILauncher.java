package pcd.assignment2.eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CLIAgent extends AbstractVerticle {
    private Path rootDir;
    private int maxSourcesToTrack;
    private int nBands;
    private int maxLoC;

    CLIAgent(Path rootDir, int maxSourcesToTrack, int nBands, int maxLoC) {
        this.rootDir = rootDir;
        this.maxSourcesToTrack = maxSourcesToTrack;
        this.nBands = nBands;
        this.maxLoC = maxLoC;
    }

    @Override
    public void start() throws Exception {
        Flag stopFlag = new AtomicBooleanFlag();
        SourceAnalyserLib lib = new SourceAnalyserLib(this.getVertx(), stopFlag);
        this.getAndPrintReport(lib);
    }

    private void getAndPrintReport(SourceAnalyser lib) {
        lib.getReport(this.rootDir, new String[]{"java","c","h"}, this.maxSourcesToTrack, this.nBands, this.maxLoC)
                .onSuccess(rep -> {
                    rep.dumpTopFilesRanking();
                    rep.dumpDistribution();
                    System.out.println("Elapsed time: " + rep.getElapsedTime() + " ms.");
                    this.getVertx().close();
                });
    }
}
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
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new CLIAgent(rootDir, topN, NI, MAXL));


    }
}
