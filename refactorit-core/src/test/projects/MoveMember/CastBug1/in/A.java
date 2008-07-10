public class A {
  public boolean isMain(BinMethod method) {
     return "main".equals(method.getName());
  }

  public void method1() {
    BinMember member = new BinMethod();
    isMain((BinMethod)member);
  }
}

class BinMember {
}

class BinMethod extends BinMember {
  public String getName() {
    return "name";
  }
}

