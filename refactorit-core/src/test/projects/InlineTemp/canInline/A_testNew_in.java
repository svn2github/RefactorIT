package test.projects.InlineTemp.canInline;

import java.util.ArrayList;

class A{
	void m(int i){
		Object o = new ArrayList(new ArrayList());
    System.out.println(o);
    System.out.println("" + o);
    System.out.println(o.toString());
	}
}