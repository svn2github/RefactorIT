
class T1528s17 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (true ? "foo" : "bar") == "foo") ? 1 : 0):
            case (("" + (false ? "foo" : "bar") == "bar") ? 2 : 0):
        }
    }
}
