package pcd.assignment2.common.gui;

public interface InputListener {

	void started(String selectedDirPath, int maxFiles, int nBands, int maxLoc);
	
	void stopped();
}
