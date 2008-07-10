interface I {

}

class A implements I {
  A(I b) {
	 
  }
  
}

class B extends A {
  static void test() {
	  I b1=null;
	  new A(b1) { };

  }


}

