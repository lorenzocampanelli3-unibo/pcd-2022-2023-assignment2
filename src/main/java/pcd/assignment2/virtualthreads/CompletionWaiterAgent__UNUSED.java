package pcd.assignment2.virtualthreads;

import pcd.assignment2.common.AnalysisUpdateListener;
import pcd.assignment2.common.Flag;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class CompletionWaiterAgent__UNUSED extends Thread {
    ScheduledExecutorService listenersUpdater;
    List<AnalysisUpdateListener> listeners;
    Flag stopFlag;

    public CompletionWaiterAgent__UNUSED(ScheduledExecutorService listenersUpdater, long listenersUpdatePeriod, List<AnalysisUpdateListener> listeners, Flag stopFlag) {
        this.listenersUpdater = listenersUpdater;
        this.listeners = listeners;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        super.run();
    }
}
