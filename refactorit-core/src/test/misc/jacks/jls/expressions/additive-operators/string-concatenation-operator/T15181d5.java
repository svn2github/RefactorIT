
class T15181d5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Double.MAX_VALUE == "1.7976931348623157E308") ? 1 : 0):
        }
    }
}
