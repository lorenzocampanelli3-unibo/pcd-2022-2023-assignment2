package pcd.assignment2.executors;

public abstract class BasicAgent extends Thread {

	protected BasicAgent(String name) {
		super(name);
	}
	
	protected void logDebug(String msg) {
		// System.out.println("[ " + getName() +"] " + msg);
	}

	protected void log(String msg) {
		System.out.println("[ " + getName() +"] " + msg);
	}
}
