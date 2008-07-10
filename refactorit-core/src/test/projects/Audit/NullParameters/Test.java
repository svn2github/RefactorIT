public class Test {
  public Integer field = null;

  public void method1(Integer a) {
  }

  public void method2(Integer a, Integer b) {

  }

  /**
   * @audit NullParametersViolation
   * @audit NullParametersViolation
   * @audit NullParametersViolation
   * @audit NullParametersViolation
   */
  public void method3() {
    method1(field);
    method1(null);
    method2(null, new Integer(1));
    Integer a = null;
    Integer b = a;
    Integer c = b;
    method1 (a);
    a = new Integer(1);
    b = a;
    method1(a);
    method1(b);
    method1(c);
  }
}
