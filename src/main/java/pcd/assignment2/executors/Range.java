package pcd.assignment2.executors;

import java.util.Objects;

public class Range implements Comparable<Range> {
    private int min;
    private int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean contains(int value) {
        return (value >= min && value <= max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return min == range.min && max == range.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public int compareTo(Range o) {
        int minComparison = Integer.compare(this.getMin(), o.getMin());
        return minComparison == 0 ? Integer.compare(this.getMax(), o.getMax()) : minComparison;
    }

    @Override
    public String toString() {
        return "[" + min + " , " + max + "]";
    }
}
