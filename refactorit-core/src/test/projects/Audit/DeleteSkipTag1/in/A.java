package DeleteSkipTag1.in;

public class A {
  private int field;

  /** @refactorit.skip shading */
  public void f1(int field) {
    this.field = field;
  }

  /**
   * javadoc comment
   *@refactorit.skip shading
   */
  public void f2(int field) {
    this.field = field;
  }

  /** javadoc comment
   * @refactorit.skip shading */
  public void f3(int field) {
    this.field = field;
  }

  /** javadoc comment
   * comment
   * @refactorit.skip shading */
  public void f4(int field) {
    this.field = field;
  }

  /*
   * comment
   */
  /** @refactorit.skip shading */
  public void f5(int field) {
    this.field = field;
  }

  /**
   * javadoc comment
   * comment @skip shading comment
   */
  public void f6(int field) {
    this.field = field;
  }
}
