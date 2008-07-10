package test.projects.InlineTemp.canInline;

import java.util.Collections;

class A{
	void m(int i){
		Object o = Collections.singletonList("asd" + '.' + "asd");
    System.out.println(o);
    System.out.println("" + o);
    System.out.println(o.toString());
	}
}