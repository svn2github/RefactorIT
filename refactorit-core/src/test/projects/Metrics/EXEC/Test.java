class Test {
  String a() {
    System.out.println(getText());	// one exec statement
    int a = 0;
    int b = 0;	
    getText();				// one exec statement
    for (String tmp = getText(); tmp.equals("13"); a++, b++) {	// two exec statements
      a++;				// one exec statement
      if (a > 1) {
        b++;				// one exec statement
      }

      if (getText().equals("Hello!")) {
      }
    }
    a+b;				// one exec statement
    getText().equals("hello"); 		// one exec statement

    String s = getText(); 
    
    s = getText();			// one exec statement
	
    return getText();
  }

  String getText() {
    return "Hello!";
  }
}