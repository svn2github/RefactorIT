
public class Test {

  private boolean testExtract(int x, int y) {
    int lasty = 43;

    int cury;

    if (/*]*/newmethod(y, lasty)/*[*/) {
      return true;
    }
    return false;
  }

  boolean newmethod(final int y, final int lasty) {
    int cury;
    return y < cury || y >= lasty;
  }
}
