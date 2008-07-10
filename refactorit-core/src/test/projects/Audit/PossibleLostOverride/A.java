package possibleLostOverride;

public class A{
	int myMeth() { 
		return 0;
	}

	int myMeth2() {
    return 0;
	}
	
	int myMeth3() {
    return 0;
	}
  
  int myMeth3(int b) { // overridden
    return 0;
  }
}

class B extends A{
  /**
   * @audit LostOverrideViolation
   */
	int myMeth(int a) {
		return 0;
	}

  int myMeth2() { // override
    return 0;
  }
  
  /**
   * 
   */
  int myMeth2(int b) {
    return 0;
  }
  
  /**
   * 
   */
  int myMeth3(int c) {
    return 0;
  }
}


class C {
  private void myMeth() {
  }
  
  public void myMeth1() {
  }
  
  protected void myMeth2() {
  }
}

class D extends C {
  
}

class E extends D {
  public void myMeth() {
  }
  /**
   * @audit LostOverrideViolation
   */
  public void myMeth1(int i) {
    
  }
  
  /**
   * @audit LostOverrideViolation
   */
  public void myMeth2(int i) {
    
  }
}