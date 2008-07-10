class Test {
  static final int a = 13;
  private static final int b = 13;

  private class Inner {
    private static final int b = 14;

    {
      class Local {
        {
          System.out.println(a);
          System.out.println(b);
        }

        class LocalInner {
          {
            System.out.println(a);
            System.out.println(b);
          }
        }
      }

      System.out.println(a);
      System.out.println(Test.a);
      System.out.println(Test.this.a);
      System.out.println(b);
      System.out.println(Test.b);
      System.out.println(Test.this.b);
    }
  }
}

class Subclass extends Test {
  {
    System.out.println(a);
  }
}
