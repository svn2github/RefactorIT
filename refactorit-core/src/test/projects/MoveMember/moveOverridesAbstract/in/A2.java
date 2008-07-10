package moveOverridesAbstract;


class C2 {
 public boolean isTrue(Object obj) {
    return false;
  }
}

interface Test2 {
 boolean isTrue(Object obj);
}
public class A2 extends C2 implements Test2 {

   public boolean isTrue(Object obj) {
    return true;
  }
}

class D2 {

}
