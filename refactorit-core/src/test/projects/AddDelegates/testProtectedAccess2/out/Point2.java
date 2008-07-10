package points2;

import points.*;


public class Point2 extends Point {


    public int hashCode() {
        return point.hashCode();
    }

    protected void finalize() throws Throwable {
        point.finalize();
    }

    public String toString() {
        return point.toString();
    }

    public boolean equals(Object obj) {
        return point.equals(obj);
    }

    protected Object clone() throws CloneNotSupportedException {
        return point.clone();
    }
}
