
class T15262or3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i |= foo();
	}
    
}
