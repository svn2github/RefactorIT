package test.projects.InlineTemp.canInline;

class A{
	void m(int i){
		Object x = new Object[]{null}[0];
    System.out.println(x);
	}
}