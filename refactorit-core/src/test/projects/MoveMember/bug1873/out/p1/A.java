package p1;

public class A {
  public B catchClause;

  public static void f(A a, B catchClause) {
    a.catchClause = catchClause;
  }
}
