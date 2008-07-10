
class T8851q4 {
    class Inner {}
}
class Sub4 extends T8851q4.Inner {
    Sub4(T8851q4 t) {
        // using a parameter is legal
        t.super();
    }
}
    