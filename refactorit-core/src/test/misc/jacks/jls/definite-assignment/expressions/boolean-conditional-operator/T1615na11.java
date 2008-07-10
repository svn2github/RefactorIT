
class T1615na11 {
    
	final boolean x;
	{ x = true; }
	void foo() {
	    if (true ? true : (x = true));
	}
    
}
