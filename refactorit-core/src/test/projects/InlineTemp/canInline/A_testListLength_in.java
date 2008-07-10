package test.projects.InlineTemp.canInline;
class A {
	void f(java.util.List l) {
    int length = l.size();
		for (int i= 0; i < length; i++);
	}
}
