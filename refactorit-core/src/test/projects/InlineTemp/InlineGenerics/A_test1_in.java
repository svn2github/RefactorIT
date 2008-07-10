package p;

import java.util.List;
import java.util.ArrayList;

class A {
  ArrayList l = new ArrayList();
  List<String> list = l;
  public void m(){
	String s = list.get(0).substring(0);
  }
}
