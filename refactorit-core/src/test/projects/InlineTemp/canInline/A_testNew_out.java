package test.projects.InlineTemp.canInline;

import java.util.ArrayList;

class A{
	void m(int i){
    System.out.println(new ArrayList(new ArrayList()));
    System.out.println("" + new ArrayList(new ArrayList()));
    System.out.println(new ArrayList(new ArrayList()).toString());
	}
}