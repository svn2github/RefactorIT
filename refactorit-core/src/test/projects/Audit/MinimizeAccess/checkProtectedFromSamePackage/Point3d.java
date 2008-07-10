package checkProtectedFromSamePackage;
class Point {
  /**
   * @audit MinimizeAccessViolation
   */
   public int x;
   /**
    * @audit MinimizeAccessViolation
    */
   public int y;
        private void warp(Point3d a) {
                if (a.z > 0)
                        a.delta(this);
        }
}

public class Point3d extends Point {
  /**
   * @audit MinimizeAccessViolation
   */
        public int z;
  /**
   * @audit MinimizeAccessViolation
   */
        public void delta(Point p) {
                p.x += this.x;          // compile-time error: cannot access p.x
                p.y += this.y;          // compile-time error: cannot access p.y
        }
  /**
   * @audit MinimizeAccessViolation
   */
        public void delta3d(Point3d q) {
                q.x += this.x;
                q.y += this.y;
                q.z += this.z;
        }
}
