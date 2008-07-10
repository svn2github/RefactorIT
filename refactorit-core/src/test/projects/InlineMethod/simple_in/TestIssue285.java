public class A {
  int method1() {
    return    /*[*/method2()/*]*/;
  }

  int method2() {
    return 1;
  } 	
}