public class Test {
  
  public void test(final int a) {
    new Object() {
      public void usesA() {
        newmethod();
      }

      void newmethod() {
        /*[*/
        System.out.println(a);
        /*]*/
      }
    };
  }
  
}
