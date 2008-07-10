// case when function with same signature exists
interface I {
 void m(I i);


}

class A implements I {
 A(A a) {
 }
 A(I i) {
 }
 void m(A a) {
 }
 void m(I i) {
 }

}

