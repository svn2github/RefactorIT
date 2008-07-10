public class A {

  public void method1() {
    BinMember member = new BinMethod();
    ((BinMethod)member).isMain();
  }
}

class BinMember {
}

class BinMethod extends BinMember {
  public String getName() {
    return "name";
  }

  public boolean isMain() {
     return "main".equals(getName());
  }
}

