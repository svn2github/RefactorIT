package p;

import java.util.List;
import java.util.ArrayList;

class A {
  ArrayList l = new ArrayList();
  public void m(){
	String s = ((List<String>) l.get(0)).get(0);
  }
}
