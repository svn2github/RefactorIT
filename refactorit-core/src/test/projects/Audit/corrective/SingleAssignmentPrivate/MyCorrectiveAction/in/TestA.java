/**
 * @violations 4
 * @author Dmitri Grintshak
 */

public class A {
  private String str; // can`t make final!

  private String str2; // can make final!

  public A() {
    this("abc");
    str = "abc";
  }

  public A(int a) {
    str2 = "abc" + a; // doesn`t call this(String), so field is assigned only
                      // once
  }

  public A(String stri) {
    str = "abc";
    str2 = "abc";
  }
}

class B {
  private int a = 10, b = 5, c; // c could be final

  private static boolean d, e; // Static - no finals

  private float h, i, j; // h could be final

  public String k, l, m; // Public - no finals

  private String n, o, p; // No finals

  private Integer t;

  private Integer u;

  private Integer z;

  public B(int x) throws NullPointerException {
    if (t == null)
      throw new NullPointerException();
    t = new Integer(4);
    u = new Integer(0);
    if (true)
      z = new Integer(0);
    a = 10;
    c = 12;
    i = 0.0f;
    k = "x";
    a = mx(x);
    h = 0.0f;
  }

  public B(String x) {
    t = new Integer(10);
    u = new Integer(0);
    z = new Integer(0);
    h = 15.0f;
    c = 6;
    i = 10.0f;
    j = 0.0f;
    while (c == 7) {
      j = 10.0f;
      o = "abc";
    }
    n = "cba";
    o = "abc";
  }

  public int mx(int x) {
    i = 10;
    return 5;
  }
}