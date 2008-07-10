public class TestA_3 {

  public static void hh() {
    Inner.toInline();
    Inner.innerZ = 1;
  }

  public void h2() {
    hh();
  }

  static class Inner {
    public static int innerZ;
    public static void toInline() {
      new InnerInner.Inner2() {};
      TestA_3 zzz = new TestA_3();
      InnerInner.Inner2.he2();
      new InnerInner();
    }

    void f() {
      toInline();
      InnerInner.Inner2.jj(3, 4, 5);
    }

    static class InnerInner {
      static class Inner2 {

        public static void he() {
          new InnerInner();
          int a = 2;
          int b = 3;
          int c = 4;
          a = b;
          b = c;
          c = a;
        }

        public static void he2() {
          he();
          jj(1, 2, 3);
        }

        public static void jj(int a, int b, int c) {
          a = b;
          b = c;
          c = a;

        }
      }
    }
  }
}
