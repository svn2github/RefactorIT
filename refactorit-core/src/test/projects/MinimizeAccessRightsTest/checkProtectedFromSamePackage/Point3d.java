
class Point {
      public int x, y;
        private void warp(Point3d a) {
                if (a.z > 0)
                        a.delta(this);
        }
}

public class Point3d extends Point {
        public int z;
        public void delta(Point p) {
                p.x += this.x;          // compile-time error: cannot access p.x
                p.y += this.y;          // compile-time error: cannot access p.y
        }
        public void delta3d(Point3d q) {
                q.x += this.x;
                q.y += this.y;
                q.z += this.z;
        }
}
