package test.projects.InlineTemp.canInline;
class A{
	void m(int i){
		long x = System.currentTimeMillis();
    System.out.println(x);
    System.out.println(x + 1);
	}
}