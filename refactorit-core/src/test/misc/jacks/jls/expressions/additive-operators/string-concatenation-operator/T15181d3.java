
class T15181d3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Double.NEGATIVE_INFINITY == "-Infinity") ? 1 : 0):
            case (("" + Double.POSITIVE_INFINITY == "Infinity") ? 2 : 0):
        }
    }
}
