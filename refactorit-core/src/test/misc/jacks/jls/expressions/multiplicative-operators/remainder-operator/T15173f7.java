
class T15173f7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY !=
            Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY) ? 1 : 0):
            case ((Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY !=
            Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY) ? 2 : 0):
            case ((Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY !=
            Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY) ? 3 : 0):
            case ((Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY !=
            Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY) ? 4 : 0):
        }
    }
}
