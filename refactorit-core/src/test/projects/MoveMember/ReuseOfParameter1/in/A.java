public class A {
  public void Main1(BinMethod method) {
     BinMethod tmp1 = method;
     BinMethod tmp2;
     tmp2 = method;
     System.out.println("method:" + method);
     boolean result = "main".equals(method.getName());

     new BinMethod().f(method);
  }

  public void Main2(BinMethod method) {
    BinMethod tmp = new BinMethod();
    method = tmp;
  }
}

class BinMethod {
  public String getName() {
    return "name";
  }

  public void f(BinMethod method) {
  }
}
