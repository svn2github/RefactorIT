
class T15155i1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((~0 == -0-1) ? 1 : 0):
        }
    }
}
