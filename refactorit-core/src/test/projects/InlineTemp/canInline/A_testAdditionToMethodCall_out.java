package test.projects.InlineTemp.canInline;
class A{
	void m(int i){
    System.out.println(System.currentTimeMillis() + 1);
    System.out.println((System.currentTimeMillis() + 1) + 1);
	}
}