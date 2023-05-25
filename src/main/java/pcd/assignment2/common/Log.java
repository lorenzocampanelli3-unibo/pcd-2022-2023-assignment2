package pcd.assignment2.common;

public class Log {
    public static void log(String msg){
        synchronized(System.out){
            System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
        }
    }
}
