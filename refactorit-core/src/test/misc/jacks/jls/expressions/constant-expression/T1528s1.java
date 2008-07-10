
class T1528s1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" == "") ? 1 : 0):
            case (("hello, world" == "hello, world") ? 2 : 0):
        }
    }
}
