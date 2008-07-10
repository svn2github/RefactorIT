
class T1528i3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + ("" instanceof String) == "true") ? 1 : 0):
        }
    }
}
