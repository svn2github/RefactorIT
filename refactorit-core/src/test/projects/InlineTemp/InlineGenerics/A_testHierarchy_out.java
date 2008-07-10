package p;

import java.util.List;
import java.util.ArrayList;

class A {
  public void m(){
	String s = new C1().getPropertyA1(); // doesn`t depend on C1$TC
	Integer i1 = ((List<Integer>) new C1().getPropertyB1()).get(0); // depends on C1$TC
	Integer i2 = (Integer) new C1().getPropertyC1();	// depends on C1$TC
  }
}

class A1<TA> {
	TA getPropertyA1(){
		return null;
	}
}

class B1<TB> extends A1<String> {
	TB getPropertyB1(){
		return null;
	}
}

class C1<TC> extends B1<List<TC>> {
	TC getPropertyC1(){
		return null;
	}
}
