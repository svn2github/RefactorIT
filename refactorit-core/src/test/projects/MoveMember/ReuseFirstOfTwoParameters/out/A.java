
public class A {
}

class B {
  String name = "bbb";

  public int compare(B b2) {
    return getName().compareTo(b2.getName());
  }

  private String getName() {
    return this.name;
  }
}
