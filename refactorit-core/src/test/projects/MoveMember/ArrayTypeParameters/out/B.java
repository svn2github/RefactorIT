package p1;

public class B {
  private B[] children = new B[5];

  public B() {
  }

  public B[] getChildren() {
    return children;
  }

  protected boolean method(final B second) {
    B[] firstChildren = getChildren();
    B[] secondChildren = second.getChildren();
    for (int i = 0; i < firstChildren.length; i++) {
      firstChildren[i].method(secondChildren[i]);
    }
  }
}
