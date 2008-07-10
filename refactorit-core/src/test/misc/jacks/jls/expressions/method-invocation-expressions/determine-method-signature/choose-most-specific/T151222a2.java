
interface A {
    void foo(String s);
}
interface B {
    void foo(Object o);
}
abstract class T151222a2 implements A, B {
    {
	foo("");
    }
}
    