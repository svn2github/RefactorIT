package UnusedAssignment;


public class B {
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test1() {
    String str = "abc";
    str = "def";
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test2() {
    String str = "abc";
    str = str.substring(0, 1);
    str = "def";
  }
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test3(String str) {
    str = "abc";
    str = "def";
  }
}
