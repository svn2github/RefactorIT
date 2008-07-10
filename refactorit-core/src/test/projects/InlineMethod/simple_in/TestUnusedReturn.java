public class A {
    int foo() {
        Object temp;
        /*[*/bar()/*]*/;
        return 0;
    }

    int bar() {
        return Integer.parseInt("4") + 1;
    }
}
