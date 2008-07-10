
class T1528s15 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (true ? 1 : 2) == "1") ? 1 : 0):
        }
    }
}
