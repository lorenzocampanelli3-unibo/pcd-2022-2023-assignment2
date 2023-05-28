package pcd.assignment2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import pcd.assignment2.common.AnalysisReport;
import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;
import pcd.assignment2.eventloop.SourceAnalyserEvLoopLib;
import pcd.assignment2.executors.SourceAnalysisServiceExec;
import pcd.assignment2.reactive.SourceAnalyserRxLib;
import pcd.assignment2.virtualthreads.SourceAnalysisServiceVT;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PerformanceTests {

    private final int numIterations;
    private final String rootDir;
    private final int maxLoC;
    private final int nBands;
    private final int maxSourcesToTrack;
    private final String[] extensions;
    private final Path rootDirPath;

    private long[] execTimes;
    private long[] vtTimes;
    private long[] evLoopTimes;
    private long[] reactiveTimes;

    private double execAvg;
    private double vtAvg;
    private double evLoopAvg;
    private double reactiveAvg;

//    private SourceAnalysisServiceExec execService;
//    private SourceAnalysisServiceVT vtService;
//    private SourceAnalyserEvLoopLib evLoopLib;
//    private SourceAnalyserRxLib rxLib;


    public PerformanceTests(int numIterations, String rootDir, int maxLoC, int nBands, int maxSourcesToTrack, String[] extensions) {
        this.numIterations = numIterations;
        this.rootDir = rootDir;
        this.maxLoC = maxLoC;
        this.nBands = nBands;
        this.maxSourcesToTrack = maxSourcesToTrack;
        this.extensions = extensions;
        this.rootDirPath = Paths.get(this.rootDir);
        this.execTimes = new long[numIterations];
        this.execAvg = 0;
        this.vtTimes = new long[numIterations];
        this.vtAvg = 0;
        this.reactiveTimes = new long[numIterations];
        this.reactiveAvg = 0;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int nIterations = 10;
        String rootDir = "E:\\linux-master";
        int maxLoC = 5000;
        int nBands = 21;
        int maxSourcesToTrack = 15;
        String[] extensions = new String[]{"java", "c", "h"};
        System.out.println("\n\nStarting tests with params:\n" +
                "rootDir = " + rootDir + ", MAXL: " + maxLoC + ", NI: " + nBands + " topN: " + maxSourcesToTrack + "\n\n");
        PerformanceTests tests = new PerformanceTests(nIterations, rootDir, 5000, 21, 15, extensions);
        long t0 = System.currentTimeMillis();
        System.gc();
        waitFor(1000);
        tests.testExecutors();
        System.gc();
        waitFor(1000);
        tests.testVirtualThreads();
        System.gc();
        waitFor(1000);
        tests.testEventLoop();
        System.gc();
        waitFor(1000);
        tests.testReactive();
        System.gc();
        waitFor(1000);
        long t1 = System.currentTimeMillis();
        System.out.println("Tests completed in " + (t1-t0) + " ms.");
    }

    private void testExecutors() throws ExecutionException, InterruptedException {
        int nWorkers = Runtime.getRuntime().availableProcessors() * 4;
        SourceAnalysisServiceExec sourceAnalysisService = new SourceAnalysisServiceExec(nWorkers);
        System.out.println("===== EXECUTORS TEST =====");
        System.out.println("Executors warmup.");
        Future<AnalysisReport> reportFuture = sourceAnalysisService.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC);
        AnalysisReport report = reportFuture.get();
        System.out.println("Executors warmup completed.");
        for (int i = 0; i < numIterations; i++) {
            System.out.println("Iter # " + i + " started.");
            reportFuture = sourceAnalysisService.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC);
            report = reportFuture.get();
            execTimes[i] = report.getElapsedTime();
            System.out.println("Iter # " + i + " completed in " + execTimes[i] + " ms.");
        }
        execAvg = Arrays.stream(execTimes).average().orElse(Double.NaN);
        sourceAnalysisService.shutdown();
        System.out.println("Executors test completed. Avg time: " + execAvg + "\n\n");
    }

    private void testVirtualThreads() throws ExecutionException, InterruptedException {
        SourceAnalysisServiceVT sourceAnalysisService = new SourceAnalysisServiceVT();
        System.out.println("===== VIRTUAL THREADS TEST =====");
        System.out.println("Virtual Threads warmup.");
        Future<AnalysisReport> reportFuture = sourceAnalysisService.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC);
        AnalysisReport report = reportFuture.get();
        System.out.println("Virtual Threads warmup completed.");
        for (int i = 0; i < numIterations; i++) {
            System.out.println("Iter # " + i + " started.");
            reportFuture = sourceAnalysisService.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC);
            report = reportFuture.get();
            vtTimes[i] = report.getElapsedTime();
            System.out.println("Iter # " + i + " completed in " + vtTimes[i] + " ms.");
        }
        vtAvg = Arrays.stream(vtTimes).average().orElse(Double.NaN);
        sourceAnalysisService.shutdown();
        System.out.println("Virtual Threads test completed. Avg time: " + vtAvg + "\n\n");
    }

    private void testEventLoop() throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        CompletableFuture<EventLoopTestResults> testResultsFuture = new CompletableFuture<>();
        System.out.println("===== EVENT LOOP TEST (VERT.X) =====");
        vertx.deployVerticle(new EventLoopTestAgent(rootDirPath, maxSourcesToTrack, nBands, maxLoC, numIterations, testResultsFuture));
        EventLoopTestResults results = testResultsFuture.get();
        this.evLoopTimes = results.getEvLoopTimes();
        this.evLoopAvg = results.getEvLoopAvg();
        vertx.close();
    }

    private void testReactive() {
        Flag stopFlag = new AtomicBooleanFlag();
        SourceAnalyserRxLib lib = new SourceAnalyserRxLib(stopFlag);
        System.out.println("===== REACTIVE TEST (RXJAVA) =====");
        System.out.println("Reactive warm up started.");
        lib.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC)
                .subscribe(r -> {
                    System.out.println("Reactive warm up completed.");
                });
        for (int i = 0; i < numIterations; i++) {
            System.out.println("Iter # " + i + " started.");
            int index = i;
            lib.getReport(rootDirPath, extensions, maxSourcesToTrack, nBands, maxLoC)
                    .subscribe(r -> {
                        reactiveTimes[index] = r.getElapsedTime();
                        System.out.println("Iter # " + index + " completed in " + reactiveTimes[index] + " ms.");
                    });
        }
        reactiveAvg = Arrays.stream(reactiveTimes).average().orElse(Double.NaN);
        System.out.println("Reactive test completed. Avg time: " + reactiveAvg + "\n\n");
    }

    private static void waitFor(long ms) throws InterruptedException {
        System.out.println("Going to sleep for " + ms + " ms.");
        Thread.sleep(ms);
    }

    private class EventLoopTestResults {
        private int maxIterations;
        private long[] evLoopTimes;
        private double evLoopAvg;;

        EventLoopTestResults(int maxIterations) {
            this.maxIterations = maxIterations;
            evLoopTimes = new long[maxIterations];
            evLoopAvg = 0;
        }

        void addEvLoopTime(int iter, long time) {
            evLoopTimes[iter] = time;
        }

        void setEvLoopAvg(double avg) {
            this.evLoopAvg = avg;
        }

        long[] getEvLoopTimes() {
            return evLoopTimes.clone();
        }

        int getMaxIterations() {
            return maxIterations;
        }

        double getEvLoopAvg() {
            return evLoopAvg;
        }
    }

    private class EventLoopTestAgent extends AbstractVerticle {
        private Path rootDir;
        private int maxSourcesToTrack;
        private int nBands;
        private int maxLoC;

        private int numIterations;
        private SourceAnalyserEvLoopLib lib;

        private CompletableFuture<EventLoopTestResults> testCompletedFuture;

        private EventLoopTestResults testResults;

        EventLoopTestAgent(Path rootDir, int maxSourcesToTrack, int nBands, int maxLoC, int numIterations, CompletableFuture<EventLoopTestResults> testCompletedFuture) {
            this.rootDir = rootDir;
            this.maxSourcesToTrack = maxSourcesToTrack;
            this.nBands = nBands;
            this.maxLoC = maxLoC;
            this.numIterations = numIterations;
            this.testCompletedFuture = testCompletedFuture;
            this.testResults = new EventLoopTestResults(numIterations);
        }

        @Override
        public void start() throws Exception {
            Flag stopFlag = new AtomicBooleanFlag();
            lib = new SourceAnalyserEvLoopLib(this.getVertx(), stopFlag);
            this.startTest();
        }

        private void startTest() {
            System.out.println("Event Loop warmup started.");
            lib.getReport(rootDir, extensions, maxSourcesToTrack, nBands, maxLoC)
                    .onSuccess(rep -> {
                        System.out.println("Event Loop warmup completed.");
                        startTestOrTerminate(0);
                    });
        }
        private void startTestOrTerminate(int currIter) {
            if (currIter < numIterations) {
                System.out.println("Iter # " + currIter + " started.");
                lib.getReport(rootDir, extensions, maxSourcesToTrack, nBands, maxLoC)
                        .onSuccess(rep -> {
                            long elapsedTime = rep.getElapsedTime();
                            testResults.addEvLoopTime(currIter, elapsedTime);
                            System.out.println("Iter # " + currIter + " completed in " + elapsedTime + " ms.");
                            startTestOrTerminate(currIter + 1);
                        });
            } else {
                double avg = Arrays.stream(testResults.getEvLoopTimes()).average().orElse(Double.NaN);
                testResults.setEvLoopAvg(avg);
                System.out.println("Event Loop test completed. Avg time: " + avg + "\n\n");
                testCompletedFuture.complete(testResults);
//                vertx.undeploy(this.deploymentID());
            }
        }
    }
}
