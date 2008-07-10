
public class A {

  public int compare(B b1, B b2) {
    return getName(b1).compareTo(getName(b2));
  }

  private String getName(B b) {
    return b.name;
  }
}

class B {
  String name = "bbb";
}
