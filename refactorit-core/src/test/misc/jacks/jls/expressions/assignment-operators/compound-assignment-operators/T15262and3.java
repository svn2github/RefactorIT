
class T15262and3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i &= foo();
	}
    
}
