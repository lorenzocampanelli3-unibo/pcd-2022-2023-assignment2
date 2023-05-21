package pcd.assignment2.eventloop;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanFlag implements Flag {
    private AtomicBoolean flag;

    public AtomicBooleanFlag() {
        this.flag = new AtomicBoolean();
    }

    @Override
    public boolean isSet() {
        return this.flag.get();
    }

    @Override
    public void set() {
        this.flag.set(true);
    }

    @Override
    public void reset() {
        this.flag.set(false);
    }

}
