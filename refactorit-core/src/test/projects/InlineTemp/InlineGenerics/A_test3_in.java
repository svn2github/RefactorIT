package p;

import java.util.List;
import java.util.ArrayList;

class A {
  ArrayList l = new ArrayList();
  List<List<String>> listOfStrings = l;
  public void m(){
	String s = listOfStrings.get(0).get(0);
  }
}
