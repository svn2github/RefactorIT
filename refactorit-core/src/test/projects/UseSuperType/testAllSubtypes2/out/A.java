interface I {
}

class A implements I {
  
}

class B extends A {
  I a;

  public void m() {
	 I b=null;
	 a=b;
  }


}

