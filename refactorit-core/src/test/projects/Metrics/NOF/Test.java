import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;


class Test {
  void a(int var1, double var2, float var3) {
    var3 = (int) var1 + (int) var2;
  }

  public final List b() {
    final class Inner {
      boolean bField;
    }
    System.out.println();

    int a = 1;

    return null;
  }

  private void c(final boolean mouseSupport) {
    if (mouseSupport) {
      MouseListener mouseListener = new MouseAdapter() {
          private int lastClickIndex = -1;
          private int minimumCheckboxWidth = 0;
        };
    }
  }
}
