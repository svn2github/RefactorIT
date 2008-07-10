
class T1528i1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((null instanceof Object) ? 1 : 0):
        }
    }
}
