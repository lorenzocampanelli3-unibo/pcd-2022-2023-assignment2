package pcd.assignment2.executors;

import pcd.assignment2.common.Flag;
import pcd.assignment2.common.UpdateTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class AnalysisMasterAgent__UNUSED extends BasicAgent__UNUSED {

    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutorService;
    private UpdateTask updateTask;
    private SrcDiscoveryTask rootTask;

    private Flag stopFlag;
    public AnalysisMasterAgent__UNUSED(ExecutorService executor, ScheduledExecutorService scheduledExecutorService, UpdateTask updateTask, SrcDiscoveryTask rootTask, Flag stopFlag) {
        super("AnalysisMasterAgent");
        this.executor = executor;
        this.scheduledExecutorService = scheduledExecutorService;
        this.updateTask = updateTask;
        this.rootTask = rootTask;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        super.run();
    }
}
