package points2;

import points.*;

// test can't access protected
public class Point2 extends Point {
 Point myPoint;


 public void test2() {
     myPoint.test2();
 }

 public int hashCode() {
     return myPoint.hashCode();
 }

 public String toString() {
     return myPoint.toString();
 }

 public boolean equals(Object obj) {
     return myPoint.equals(obj);
 }
}
