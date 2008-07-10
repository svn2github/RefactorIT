
class T1528p16 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((true ? true : false) ? 1 : 0):
        }
    }
}
