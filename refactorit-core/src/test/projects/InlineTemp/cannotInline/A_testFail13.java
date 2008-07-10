package p;
class A {
	void f() {
		for (int i= 0; i < 4; i++); // modified -- Eclipse tests did not have the "i++"
	}
}
