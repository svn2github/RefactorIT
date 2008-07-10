
class T1528e2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("The integer " + Long.MAX_VALUE +
    " is mighty big." == "The integer 9223372036854775807 is mighty big.") ? 1 : 0):
        }
    }
}
