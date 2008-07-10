
class T1528div01 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (5/0) == "0") ? 1 : 0):
        }
    }
}
