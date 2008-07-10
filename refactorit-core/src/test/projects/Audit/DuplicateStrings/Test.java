public class Test {
  final String field1 = "a";

  String field2 = "b";

  /**
   * @audit DuplicateStringsViolation
   */
    public void method1() {
      System.out.println("a");
//      System.out.println("b");
    }
}
