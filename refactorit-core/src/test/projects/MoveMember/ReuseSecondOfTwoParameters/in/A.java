
public class A {

  public int compare
      // method
      (
      // brace
      String str
      // some
      ,
      // first parameter
      // another comment
      B b2
      // some
      ,
      // second parameter
      // some comment
      int i
      // third parameter
      ) {
    return str.compareTo(getName(b2));
  }

  private String getName(B b) {
    return b.name;
  }
}

class B {
  String name = "bbb";
}
