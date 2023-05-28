package pcd.assignment2.executors;

public abstract class BasicAgent__UNUSED extends Thread {

	protected BasicAgent__UNUSED(String name) {
		super(name);
	}
	
	protected void logDebug(String msg) {
		// System.out.println("[ " + getName() +"] " + msg);
	}

	protected void log(String msg) {
		System.out.println("[ " + getName() +"] " + msg);
	}
}
