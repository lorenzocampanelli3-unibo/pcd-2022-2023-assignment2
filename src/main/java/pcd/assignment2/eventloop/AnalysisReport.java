package pcd.assignment2.eventloop;

import pcd.assignment2.executors.AnalysisStatsSnapshot;
import pcd.assignment2.executors.LocEntry;
import pcd.assignment2.executors.Range;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnalysisReport {
    private AnalysisStatsSnapshot snapshot;
    private long elapsedTime;

    public AnalysisReport(AnalysisStatsSnapshot snapshot, long elapsedTime) {
        this.snapshot = snapshot;
        this.elapsedTime = elapsedTime;
    }

    public AnalysisStatsSnapshot getSnapshot() {
        return snapshot;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public Map<Range, Integer> getDistributionMap() {
        AnalysisStatsSnapshot snapshot = this.getSnapshot();
        Map<Range, Integer> distributionMap = new HashMap<>();
        int[] bands = snapshot.getBands();
        int nLocPerBand = snapshot.getMaxLoc() / (bands.length - 1);
        int a = 0;
        int b = 0;
        for (int i = 0; i < bands.length - 1; i++) {
            b = a + nLocPerBand - 1;
            distributionMap.put(new Range(a , b), bands[i]);
            a = b + 1;
        }
        distributionMap.put(new Range(a, Integer.MAX_VALUE), bands[bands.length - 1]);
        return Collections.unmodifiableMap(distributionMap);
    }

    public void dumpTopFilesRanking() {
        AnalysisStatsSnapshot snapshot = this.getSnapshot();
        for (LocEntry entry : snapshot.getRank()) {
            System.out.println(entry.getSrcPathRelativeToRoot() + " - " + entry.getNLoc());
        }
    }

    public void dumpDistribution() {
        AnalysisStatsSnapshot snapshot = this.getSnapshot();
        int[] bands = snapshot.getBands();
        int nLocPerBand = snapshot.getMaxLoc() / (bands.length - 1);
        int a = 0;
        int b = 0;
        for (int i = 0; i < bands.length - 1; i++) {
            b = a + nLocPerBand - 1;
            System.out.println("band " + (i+1) + " ["+ a + " - " + b + "] : " + bands[i]);
            a = b + 1;
        }
        System.out.println("band " + bands.length + " >= " + snapshot.getMaxLoc() + " : " + bands[bands.length - 1]);
    }

}
