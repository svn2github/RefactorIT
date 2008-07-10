package test.projects.InlineTemp.canInline;

import java.util.Collections;

class A{
	void m(int i){
    System.out.println(Collections.singletonList("asd" + '.' + "asd"));
    System.out.println("" + Collections.singletonList("asd" + '.' + "asd"));
    System.out.println(Collections.singletonList("asd" + '.' + "asd").toString());
	}
}