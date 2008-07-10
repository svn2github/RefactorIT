package p;

import java.util.List;
import java.util.ArrayList;

class A {
  ArrayList l = new ArrayList();
  List<String> list = l;
  public void m(){
	takes(list.get(0));
  }

  public void takes(Object obj){

  }

  public void takes(String str){

  }
}
