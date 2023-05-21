package pcd.assignment2.eventloop;

import pcd.assignment2.executors.AnalysisStatsSnapshot;
import pcd.assignment2.executors.LocEntry;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnalysisStats {

	// private final Lock lock = new ReentrantLock();

	private Path rootDir;
	private int[] bands;
	private LinkedList<LocEntry> maxLocSources;
	private int maxSourcesToTrack;
	private int maxLinesOfCode;
	private long nSrcProcessed;

	private long nFoldersProcessed;
	
	public AnalysisStats(Path rootDir, int maxSourcesToTrack, int nBands, int maxLinesOfCode) {
		this.rootDir = rootDir;
		maxLocSources = new LinkedList<>();
		this.maxSourcesToTrack = maxSourcesToTrack;
		this.maxLinesOfCode = maxLinesOfCode;
		bands = new int[nBands];
		Arrays.fill(bands, 0);
		nSrcProcessed = 0;
		nFoldersProcessed = 1;
	}
	
	public void clear() {
		maxLocSources.clear();
		Arrays.fill(bands, 0);
		nSrcProcessed = 0;
		nFoldersProcessed = 0;
	}
	
	public void updateFileStats(Path src, int nLoc) {
		nSrcProcessed++;
		Iterator<LocEntry> it = maxLocSources.iterator();
		int pos = 0;
		boolean toBeInserted = false;
		while (it.hasNext() && pos < maxSourcesToTrack) {
			LocEntry el = it.next();
			if (nLoc > el.getNLoc()) {
				toBeInserted = true;
				break;
			}
			pos++;
		}

		if (pos < maxSourcesToTrack || toBeInserted) {
			maxLocSources.add(pos, new LocEntry(src.toString(), rootDir.relativize(src).toString(), nLoc));
			if (maxLocSources.size() > maxSourcesToTrack) {
				maxLocSources.removeLast();
			}
		}

		if (nLoc > maxLinesOfCode) {
			bands[bands.length - 1]++;
		} else {
			int nLocPerBand = maxLinesOfCode/(bands.length - 1);
			int bandIndex = nLoc / nLocPerBand;
			bands[bandIndex]++;
		}
	}

	public void updateDirStats() {
		nFoldersProcessed++;
	}

	public void dumpSrcsWithMoreNLocs() {
		maxLocSources.forEach(el -> {
			System.out.println((el.getSrcFullPath() + " - "  + el.getNLoc()));
		});
	}

	public AnalysisStatsSnapshot getSnapshot() {
		LocEntry[] list = new LocEntry[maxLocSources.size()];
		maxLocSources.toArray(list);
		return new AnalysisStatsSnapshot(nSrcProcessed, nFoldersProcessed, list, bands.clone(),maxLinesOfCode);
	}

	public void dumpDistribution() {
		int nLocPerBand = maxLinesOfCode/(bands.length - 1);
		int a = 0;
		int b = nLocPerBand;

		for (int i = 0; i < bands.length - 1; i++) {
			System.out.println("band " + (i+1) + " (" + a + " - " + b + "): " + bands[i]);
			a = b + 1;
			b += nLocPerBand;
		}
		System.out.println("band " + bands.length + " ( >= " + a + "): " + bands[bands.length - 1]);
	}
	
}
