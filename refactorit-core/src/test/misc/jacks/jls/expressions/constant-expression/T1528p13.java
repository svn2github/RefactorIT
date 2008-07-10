
class T1528p13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((true & true) ? 1 : 0):
            case ((true ^ false) ? 2 : 0):
            case ((false | true) ? 3 : 0):
        }
    }
}
