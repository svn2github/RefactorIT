package UnusedAssignment;

public class A {
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test1() {
    int a = 0;
    a = 1;
  }
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test2() {
    int a = 0;
    a += 0;
    a = 1;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test3() {
    int a = 0;
    a += 0;
    a = 1;
    a = 2;
  }
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test4() {
    int a = value();
    a = 1;
  }
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   *
   */
  public void test5() {
    int a = value();
    a += 0;
    a = 1;
    a = 2;
  }
  
  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test6() {
    int interim = value();
    int a = interim;
    a = 1;
  }
  
  /**
   * @audit UnusedAssignment
   */
  public void test7(int a) {
    a = 1;
  }

  /**
   * @audit UnusedAssignment
   * @audit UnusedAssignment
   */
  public void test8(int a) {
    a = 1;
    a = 2;
  }
  
  /**
   * @audit UnusedAssignment a
   * @audit UnusedAssignment a
   */
  public void test9() {
    int a; // Defaults to 0!
    a = 1;
    a = 2;
  }
  
  /**
   */
  private int value() {
    return 0;
  }
}
