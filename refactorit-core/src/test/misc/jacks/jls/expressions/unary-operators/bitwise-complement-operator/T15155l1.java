
class T15155l1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((~0L == -0L-1) ? 1 : 0):
        }
    }
}
