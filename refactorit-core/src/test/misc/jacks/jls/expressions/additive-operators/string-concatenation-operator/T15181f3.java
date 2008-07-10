
class T15181f3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Float.NEGATIVE_INFINITY == "-Infinity") ? 1 : 0):
            case (("" + Float.POSITIVE_INFINITY == "Infinity") ? 2 : 0):
        }
    }
}
