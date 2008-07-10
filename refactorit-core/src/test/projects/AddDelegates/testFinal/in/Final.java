
class FinalMethod {
  public void f() { }
}

class BaseClass {
  public final void f() { }
}

class FinalTest extends BaseClass {
  FinalMethod field;

}

