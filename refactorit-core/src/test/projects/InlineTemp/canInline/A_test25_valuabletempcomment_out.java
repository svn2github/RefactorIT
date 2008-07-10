package test.projects.InlineTemp.canInline;

class A {
	void foo() {
		int x=0;//valuable temp comment
		// some valuable important comment which will be erased
		System.out.println(42);
	}
}