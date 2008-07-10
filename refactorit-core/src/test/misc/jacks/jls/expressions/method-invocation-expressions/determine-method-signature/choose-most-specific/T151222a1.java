
interface A {
    void foo(String s);
}
abstract class T151222a1 implements A {
    void foo(Object o) {}
    {
	foo("");
    }
}
    