package p1;

public class A {
  private B field = new B();

  public A(B param) {
    method(field, param);
  }

  protected boolean method(final B first, final B second) {
    B[] firstChildren = first.getChildren();
    B[] secondChildren = second.getChildren();
    for (int i = 0; i < firstChildren.length; i++) {
      method(firstChildren[i], secondChildren[i]);
    }
  }

}
