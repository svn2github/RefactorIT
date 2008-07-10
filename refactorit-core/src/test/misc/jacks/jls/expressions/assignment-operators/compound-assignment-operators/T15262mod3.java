
class T15262mod3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i %= foo();
	}
    
}
