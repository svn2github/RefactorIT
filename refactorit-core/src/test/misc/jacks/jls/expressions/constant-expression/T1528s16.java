
class T1528s16 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((true ? "1" : "") == "1") ? 1 : 0):
        }
    }
}
