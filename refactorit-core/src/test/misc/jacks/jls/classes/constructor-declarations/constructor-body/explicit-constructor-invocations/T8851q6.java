
class T8851q6 {
    class Inner {
        T8851q6 t;
    }
}
class Sub6 extends T8851q6.Inner {
    Sub6() {
        // using an inherited member is not legal
        t.super();
    }
}
    