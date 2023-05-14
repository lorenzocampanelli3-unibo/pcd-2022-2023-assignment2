package pcd.assignment2.executors;

import pcd.assignment2.executors.gui.AnalyzerView;
import pcd.assignment2.executors.gui.Controller;

public class GUILauncher {
    public static void main(String[] args) {
        String defRootDir = "E:\\linux-master";
        int defMaxLines = 5000;
        int defNBands = 21;
        int defNTopFiles = 15;
        AnalyzerView view = new AnalyzerView(defRootDir, defMaxLines, defNBands, defNTopFiles);
        Controller controller = new Controller(view);
        view.addListener(controller);

        view.display();
    }
}
