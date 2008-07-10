package pack;

import java.util.*;
import java.io.*;

public class Test { 
	List list = new ArrayList();
	List additional = new ArrayList();
  
	/**
   * @audit DangerousIteratorUsageViolation
	 */
	public void meth1() {
		Iterator it=list.iterator();
		it.next(); // violation
		it.hasNext();
	}

  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth2() {
		Iterator it=list.iterator();
		it.hasNext();
		it.next(); // violation
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth3() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			it = additional.iterator();
			it.next(); // violation
		} else {
			list.size();
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth4() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			it = additional.iterator();
			it.next(); // violation
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth5() {
		for(Iterator it=list.iterator(); it.hasNext(); ) {
			it.next();
			if(1>2) {
				it.next(); // violation
			}
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth6() {
		for(Iterator it=list.iterator(); it.hasNext(); ) {
			
			if(1>2) {
				it.next();
			}
			
			if(1>3) {
				it.next(); // violation
			}
		}
	}
  
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth7() {
		Iterator it=list.iterator();
		if(!it.hasNext()) {
			it.next(); // violation
		} else {
			list.size();
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth8() {
		Iterator it=list.iterator();
		do {
			it.next(); // violation
		} while (it.hasNext());
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   * @audit DangerousIteratorUsageViolation
   */
	public void meth9() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			if(1>3) {
				it.next();
			} else {
				it.next();
			} 
			
			if(1<4) {
				it.next(); // violation
			} else {
				it.next(); // violation
			}
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth10() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			if(1>3) {
				it.next();
				if(1>5) {
					it.next(); // violation
				}
			} else {
				it.next();
			} 
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   */
	public void meth11() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			if(1>3) {
				it.next();
				it.next(); // violation
			} else {
				it.next();
			} 
		}
	}
	
  /**
   * @audit DangerousIteratorUsageViolation
   * @audit DangerousIteratorUsageViolation
   * @audit DangerousIteratorUsageViolation
   * @audit DangerousIteratorUsageViolation
   * @audit DangerousIteratorUsageViolation
   */
	public void meth12() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			if(1>3) {
				it.next();
				it.next(); // violation
				if(1<4) it.next(); // violation
				else if(1>4 && 1==1) {
					Object obj = (1<4)? it.next() : it.next(); // violation, violation
				}
			} else {
				it.next();
				it.next(); // violation
			} 
		}
	}

	
	public void okMeth1() {
		Iterator it=list.iterator();
		do {
			if(it.hasNext()) {
				it.next();
			}
		} while (it.hasNext());
	}
	
	public void okMeth2() {
		Iterator it=list.iterator();
		if(!it.hasNext()) {
		} else {
			it.next();
		}
	}
	
	public void okMeth3() {
		Iterator it=list.iterator();
		while(it.hasNext()) {
			if(it.hasNext()) {
				it.next();
			}
			if(it.hasNext()) {
				it.next();
			}
		}
	}
	
	public void okMeth4(int a) {
		Iterator it=list.iterator();
		while(it.hasNext()) {
			if(it.hasNext()) {
				it.next();
			} else {
				if(a<4){it.next();}
				else if(a<10){it.next();}
				else if(a<11){it.next();}
				else it.next();
			}
		}
	}
	
	public void okMeth5(int a) {
		Iterator it=list.iterator();
		if(it.hasNext()) {		
			switch(a){
				case(0): it.next();
				break;
				
				case(1): it.next();
				break;
				
				default: it.next();
			}
		}
	}
	
	public void okMeth6() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			try {
			  InputStreamReader in = new FileReader("file");
			} catch(Error e) {
			  it.next();
			} catch(FileNotFoundException e){
			  it.next();
			}
		}
	}
	
	public void okMeth7() {
		Iterator it=list.iterator();
		if(it.hasNext()) {
			try {
			  InputStreamReader in = new FileReader("file");
			  it.next();
			} catch(Error e) {
			  if(1>4){
				  it.next();
			  } else if(1>29) {
				  it.next();
			  } else {
				  it.next();
			  }
			} catch(FileNotFoundException e){
			  it.next();
			} finally {
			  it.next();
			}
		}
	}
	
}