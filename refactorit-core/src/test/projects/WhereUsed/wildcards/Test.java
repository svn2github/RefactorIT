package  WhereUsedWildcards;

import java.util.*;

public class Test {
  static ArrayList<? extends MyPrimitiveType> list;
  static ArrayList list2 = new ArrayList();
  static ArrayList<MyType1> list3 = new ArrayList<MyType1>();
  static ArrayList<Map<List<Map<Integer, List<? extends List<? extends MyPrimitiveType>>>>,String>> li;

  public static void main(String[] args) {
    list2.add(new MyType1());
    list2.add(new MyType2());
    list = list2;

    for (MyPrimitiveType cls : list) {
      cls.printStr();
    }
  }
}

class MyPrimitiveType {
  public String str = "MyPrimitiveType";

  public void printStr() {
    System.out.println(str);
  }
}

class MyType1 extends MyPrimitiveType {
  public String str = "MyType1";
}

class MyType2 extends MyPrimitiveType {
  public String str = "MyType2";
}
