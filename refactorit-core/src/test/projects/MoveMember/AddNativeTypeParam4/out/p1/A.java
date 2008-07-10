package p1;

import java.awt.*;
import p2.B;

public class A extends Dialog {
  public Image buffer;
  private B b;

  public A() {
    super((Frame) null);
  }

  public void paint(Graphics g) {
    b.paintBuffer(this, g);
  }
}
