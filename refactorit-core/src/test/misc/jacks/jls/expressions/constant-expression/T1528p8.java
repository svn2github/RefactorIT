
class T1528p8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1 + 2 == 3) ? 1 : 0):
            case ((1 - 2 == -1) ? 2 : 0):
        }
    }
}
