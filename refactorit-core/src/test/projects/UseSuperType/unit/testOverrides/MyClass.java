package p1;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;


// for classpath loading

interface MyInterface extends List {
  public int size();

}
abstract class  MyClass implements MyInterface {

}

class MyClass2 extends MyClass {

  public int size() {
    return 0;
  }

}

class MyClass3 extends MyClass  {
/*  public MyLinkedList(Collection col) {
   super(col);
  }
  */
  public int size() {
   return 0;
  }
  public void f(MyInterface list) {

    if ( list.size() > 0 ) {
    }

  }




}



