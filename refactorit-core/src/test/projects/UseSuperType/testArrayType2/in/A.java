package p;

// Test parameter

class B {
 void m()  {
 }
}
class A extends B{
    A[] m1() {
      return null;
    }
    A[] g(A a[]) {
      return a[0].m1();
    }
    void f(A a3[]){

        A [] a={new  A() };

	a3[0]=new A();
	a3[1]=a[0];
	int l= a3.length;

}

}
