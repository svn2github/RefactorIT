
class T1528p11 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1 == 1) ? 1 : 0):
            case ((1 != 2) ? 2 : 0):
            case ((true == true) ? 3 : 0):
            case ((true != false) ? 4 : 0):
            case (('a' == 'a') ? 5 : 0):
            case (('a' != 'b') ? 6 : 0):
        }
    }
}
