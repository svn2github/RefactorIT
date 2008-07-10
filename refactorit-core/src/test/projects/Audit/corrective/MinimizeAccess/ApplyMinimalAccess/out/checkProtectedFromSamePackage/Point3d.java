package checkProtectedFromSamePackage;
class Point {
   int x;
   int y;
        private void warp(Point3d a) {
                if (a.z > 0)
                        a.delta(this);
        }
}

/**
 * @violations 5
 */
public class Point3d extends Point {
        int z;
        void delta(Point p) {
                p.x += this.x;          // compile-time error: cannot access p.x
                p.y += this.y;          // compile-time error: cannot access p.y
        }
        private void delta3d(Point3d q) {
                q.x += this.x;
                q.y += this.y;
                q.z += this.z;
        }
}
