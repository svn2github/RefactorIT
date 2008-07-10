package p2;

import java.util.*;
import p1.A;

public class B {
	List list; // java.util.List
	p1.List a; // p1.List
  
	p1.List getA() {
		return new A();
	}
}