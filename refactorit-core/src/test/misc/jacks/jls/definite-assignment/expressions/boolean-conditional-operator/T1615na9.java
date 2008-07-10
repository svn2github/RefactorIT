
class T1615na9 {
    
	static final boolean x;
	static { x = true; }
	void foo() {
	    if (true ? true : (x = true));
	}
    
}
