/**
  bug 2041 test
  @author tonis
*/

public class A {
  B []declaredMethods;
  public void setOwners(ARef myRef) {

    for (int i = 0; i < this.declaredMethods.length; ++i) {
      this.declaredMethods[i].setOwner(myRef);
    }

  }
}
