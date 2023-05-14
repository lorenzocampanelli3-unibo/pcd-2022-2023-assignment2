package pcd.assignment2.executors;

public class AnalysisStatsSnapshot {
	

	private long nSrcsProcessed;
	private long nDirsProcessed;
	private LocEntry[] entries;
	private int[] bands;
	private int maxLoC;
	
	public AnalysisStatsSnapshot(long nSrcsProcessed, long nDirsProcessed, LocEntry[] entries, int[] bands, int maxLoC) {
		this.nSrcsProcessed = nSrcsProcessed;
		this.nDirsProcessed = nDirsProcessed;
		this.entries = entries;
		this.bands = bands;
		this.maxLoC = maxLoC;

	}

	public long getNumSourcesProcessed() {
		return nSrcsProcessed;
	}

	public long getNumDirectoriesProcessed() {
		return nDirsProcessed;
	}
	public LocEntry[] getRank() {
		return entries;
	}

	public int[] getBands() {
		return bands;
	}
	
	public int getMaxLoc() {
		return maxLoC;
	}


}
