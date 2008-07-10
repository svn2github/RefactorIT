package p;
class A{
 void f(A a){
   A[] arr= new A[] {a}; // Modified: Eclipse did not have the "new" here
   arr[0]= null;
 }
}
