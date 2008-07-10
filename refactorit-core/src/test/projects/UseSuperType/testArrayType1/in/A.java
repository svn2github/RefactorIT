package p;

// Random tests ;)

class B {
 void m()  {
 }
}
class A extends B{
    A[] m1() {
    }
    void g(A a[]) {
      a[0].m1();
    }
    void f(A a3[]){
        a3[0].m();


        A [] a={new  A() };

	a[0].m();
	A []a1;
	A []b=a1[0].m1();
	if ( b[0].hashCode()== a[0].hashCode() ) {

	}
}

}
