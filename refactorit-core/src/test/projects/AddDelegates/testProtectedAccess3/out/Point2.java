package points2;

import points.*;

// test can acces if field is in this class
public class Point2 extends Point {
 Point2 myPoint;


 public void test2() {
     myPoint.test2();
 }

 public int hashCode() {
     return myPoint.hashCode();
 }

 protected void finalize() throws Throwable {
     myPoint.finalize();
 }

 public String toString() {
     return myPoint.toString();
 }

 public boolean equals(Object obj) {
     return myPoint.equals(obj);
 }

 protected Object clone() throws CloneNotSupportedException {
     return myPoint.clone();
 }

 protected void test() {
     myPoint.test();
 }
}
