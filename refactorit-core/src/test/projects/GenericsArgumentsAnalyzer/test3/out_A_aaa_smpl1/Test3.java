package genericsrefact.test3;

import java.util.ArrayList;
import java.util.List;

public class Test3 {
  
  public static void main(String[] args) {
    List<Number> outside1 = new ArrayList<Number>();
    outside1.add(new Float(4));
    A typeA = new A();
    B typeB = new B();
    typeA.aaa(outside1);
  }
  
}

class A {
  public void aaa(List<Number> list1){
    List<Number> smpl1 = list1;
    smpl1.add(new Double(3));
  }
}

class B extends A {
  public void aaa(List<Number> list2){
    List<Number> smpl2 = list2;
    smpl2.add(new Integer(2));
  }
}
