package p;

// Random tests ;)

class B {
 void m()  {
 }
}
class A extends B{
    B[] m1() {
    }
    void g(A a[]) {
      a[0].m1();
    }
    void f(B a3[]){
        a3[0].m();


        B [] a={new  A() };

	a[0].m();
	A []a1;
	B []b=a1[0].m1();
	if ( b[0].hashCode()== a[0].hashCode() ) {

	}
}

}
