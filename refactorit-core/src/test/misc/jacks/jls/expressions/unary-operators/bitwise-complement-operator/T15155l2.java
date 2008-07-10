
class T15155l2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((~0xffffffffffffffffL == -0xffffffffffffffffL-1) ? 1 : 0):
        }
    }
}
