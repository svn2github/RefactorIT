package p1;

import java.awt.*;
import p2.B;

public class A extends Dialog {
  public Image buffer;
  private B b;

  public A() {
    super((Frame) null);
  }

  public void paintBuffer(Graphics g) {
    g.drawImage(buffer, 0, 0, this);
  }

  public void paint(Graphics g) {
    paintBuffer(g);
  }
}
