/*
 * TestA.java
 *
 * Created on May 3, 2005, 12:45 PM
 */

/**
 *
 * @author  Arseni Grigorjev
 */
public class TestA {
  
  private int a;
  private int b;
  
  /** Creates a new instance of TestA */
  public TestA() {
  }

  /**
   * @param a
   * @param b
   */
  public TestA(int a, int b) {
    this.a = a;
    this.b = b;
  }

  /**
   *
   */
  public int getA() {
    return this.a;
  }

  /**
   *
   */
  public void setA(final int a) {
    this.a = a;
  }

  /**
   *
   */
  public int getB() {
    return this.b;
  }

  /**
   *
   */
  public void setB(final int b) {
    this.b = b;
  }
}
