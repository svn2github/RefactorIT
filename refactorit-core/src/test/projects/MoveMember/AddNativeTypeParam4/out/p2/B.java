package p2;

import java.awt.Graphics;
import p1.A;


public class B {

  public void paintBuffer(A a, Graphics g) {
    g.drawImage(a.buffer, 0, 0, a);
  }
}
