package AddSkipTag1.in;


public class A {
  private int field;

  /**
   * @refactorit.skip shading
   */
  public boolean f1(int field) {
    return this.field == field;
  }

  public void f2(int field) {
    this.field = field;
  }

  /**
   * javadoc comment
   */
  public void f3(int field) {
    this.field = field;
  }

  /** javadoc comment */
  public void f4(int field) {
    this.field = field;
  }

  /** javadoc comment
   * comment */
  public void f5(int field) {
    this.field = field;
  }

  /*
   * comment
   */
  public void f6(int field) {
    this.field = field;
  }
}
