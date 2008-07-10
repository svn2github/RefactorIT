package p;
//name clash
class A extends Supertype {
  void f(){
    int i = 0;
  }
  
  void supertypeFieldUser() {
    int user = i;
  }
}

class Supertype {
  Object i;
}