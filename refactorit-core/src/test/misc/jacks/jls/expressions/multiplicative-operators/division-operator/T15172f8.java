
class T15172f8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1f / Float.NEGATIVE_INFINITY == 0) ? 1 : 0):
            case ((1/(1f / Float.NEGATIVE_INFINITY) == Float.NEGATIVE_INFINITY) ? 2 : 0):
            case ((1f / Float.POSITIVE_INFINITY == 0) ? 3 : 0):
            case ((1/(1f / Float.POSITIVE_INFINITY) == Float.POSITIVE_INFINITY) ? 4 : 0):
            case ((-1f / Float.NEGATIVE_INFINITY == 0) ? 5 : 0):
            case ((1/(-1f / Float.NEGATIVE_INFINITY) == Float.POSITIVE_INFINITY) ? 6 : 0):
            case ((-1f / Float.POSITIVE_INFINITY == 0) ? 7 : 0):
            case ((1/(-1f / Float.POSITIVE_INFINITY) == Float.NEGATIVE_INFINITY) ? 8 : 0):
        }
    }
}
