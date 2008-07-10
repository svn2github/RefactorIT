public class A {
}

class BinMethod {
  public String getName() {
    return "name";
  }

  public void f(BinMethod method) {
  }

  public void Main1() {
     BinMethod tmp1 = this;
     BinMethod tmp2;
     tmp2 = this;
     System.out.println("method:" + this);
     boolean result = "main".equals(getName());

     new BinMethod().f(this);
  }

  public void Main2(BinMethod method) {
    BinMethod tmp = new BinMethod();
    method = tmp;
  }
}
