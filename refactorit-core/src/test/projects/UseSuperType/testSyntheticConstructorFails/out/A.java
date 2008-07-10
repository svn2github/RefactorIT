interface I {

}

class A implements I {
  A(B b) {
	  A a=b;
	 
  }
  
}

class B extends A {
  static void test() {
	  B b1=null;
	  new A(b1) { };

  }


}

