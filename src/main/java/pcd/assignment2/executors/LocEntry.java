package pcd.assignment2.executors;

public class LocEntry {
	
	private String srcFullPath;
	private String srcPathRelativeToRoot;
	
	private int nLoc;
	
	LocEntry(String srcFullPath, String srcPathRelativeToRoot,  int nLoc){
		this.srcFullPath = srcFullPath;
		this.srcPathRelativeToRoot = srcPathRelativeToRoot;
		this.nLoc = nLoc;
	}
	
	public String getSrcPathRelativeToRoot() {
		return srcPathRelativeToRoot;
	}
	
	public String getSrcFullPath() {
		return srcFullPath;
	}
	
	public int getNLoc() {
		return nLoc;
	}

}
