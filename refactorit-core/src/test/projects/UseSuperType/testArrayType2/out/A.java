package p;

// Test parameter

class B {
 void m()  {
 }
}
class A extends B{
    B[] m1() {
      return null;
    }
    B[] g(A a[]) {
      return a[0].m1();
    }
    void f(B a3[]){

        B [] a={new  A() };

	a3[0]=new A();
	a3[1]=a[0];
	int l= a3.length;

}

}
