public class Test {
  
  public void test(final int a) {
    new Object() {
      public void usesA() {
        /*[*/
        System.out.println(a);
        /*]*/
      }
    };
  }
  
}
