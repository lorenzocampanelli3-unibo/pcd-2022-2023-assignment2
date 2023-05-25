package pcd.assignment2.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class TestReactive {
    public static void main(String[] args) throws InterruptedException {
        String testPath = "E:\\linux-master";
        int maxSourcesToTrack = 15;
        int nBands = 21;
        int maxLoC = 5000;
        SourceAnalyserLib lib = new SourceAnalyserLib();
        lib.getReport(Paths.get(testPath), new String[]{"java", "c", "h"}, maxSourcesToTrack, nBands, maxLoC)
                .subscribe(r -> {
                    r.dumpTopFilesRanking();
                    r.dumpDistribution();
                    System.out.println("# dirs: " + r.getSnapshot().getNumDirectoriesProcessed());
                    System.out.println("# files: " + r.getSnapshot().getNumSourcesProcessed());
                    System.out.println("Elapsed time: " + r.getElapsedTime() + "ms.");
                });

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
}
