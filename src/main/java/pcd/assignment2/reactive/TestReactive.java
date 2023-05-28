package pcd.assignment2.reactive;

import pcd.assignment2.common.AtomicBooleanFlag;
import pcd.assignment2.common.Flag;

import java.nio.file.Paths;

public class TestReactive {
    public static void main(String[] args) throws InterruptedException {
        String testPath = "E:\\TestFolder3";
        int maxSourcesToTrack = 15;
        int nBands = 21;
        int maxLoC = 5000;
        Flag stopFLag = new AtomicBooleanFlag();
        SourceAnalyserRxLib lib = new SourceAnalyserRxLib(stopFLag);
        lib.getReport(Paths.get(testPath), new String[]{"java", "c", "h"}, maxSourcesToTrack, nBands, maxLoC)
                .subscribe(r -> {
                    r.dumpTopFilesRanking();
                    r.dumpDistribution();
                    System.out.println("# dirs: " + r.getSnapshot().getNumDirectoriesProcessed());
                    System.out.println("# files: " + r.getSnapshot().getNumSourcesProcessed());
                    System.out.println("Elapsed time: " + r.getElapsedTime() + "ms.");
                });

//        String testPath = "E:\\TestFolder3";
//        Flag stopFlag = new AtomicBooleanFlag();
//        SourceAnalyserRxLib lib = new SourceAnalyserRxLib(stopFlag);
//        var disposable = lib.analyseSources(Paths.get(testPath), new String[]{"java", "c", "h"})
//                .subscribe(ev -> {
//                    log("handler executed.");
//                    System.out.println("> " + ev);
//                });
//
//        Thread.sleep(1000);
//        stopFlag.set();
//        System.out.println("Before subscribe");
//        System.out.println("Before Thread: " + Thread.currentThread());
//
//        Observable.timer(1, TimeUnit.SECONDS, Schedulers.io())
//                .concatWith(Observable.timer(1, TimeUnit.SECONDS, Schedulers.single()))
//                .subscribe(t -> {
//                    System.out.println("Thread: " + Thread.currentThread());
//                    System.out.println("Value:  " + t);
//                });
//
//
//        System.out.println("After subscribe");
//        System.out.println("After Thread: " + Thread.currentThread());
//
//    // RxJava uses daemon threads, without this, the app would quit immediately
//        Thread.sleep(3000);
//
//        System.out.println("Done");

//        System.out.println("Before blockingSubscribe");
//        System.out.println("Before Thread: " + Thread.currentThread());
//
//        Observable.interval(1, TimeUnit.SECONDS)
//                .take(5)
//                .blockingSubscribe(t -> {
//                    System.out.println("Thread: " + Thread.currentThread());
//                    System.out.println("Value:  " + t);
//                });
//
//        System.out.println("After blockingSubscribe");
//        System.out.println("After Thread: " + Thread.currentThread());
    }

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
