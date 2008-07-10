interface I {
}

class A implements I {
  
}

class B extends A {
  A a;

  public void m() {
	 B b=null;
	 a=b;
  }


}

