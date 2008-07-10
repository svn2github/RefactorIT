
class T15173f10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0f % 1f == 0) ? 1 : 0):
            case ((1/(0f % 1f) == Float.POSITIVE_INFINITY) ? 2 : 0):
            case ((-0f % 1f == 0) ? 3 : 0):
            case ((1/(-0f % 1f) == Float.NEGATIVE_INFINITY) ? 4 : 0):
            case ((0f % -1f == 0) ? 5 : 0):
            case ((1/(0f % -1f) == Float.POSITIVE_INFINITY) ? 6 : 0):
            case ((-0f % -1f == 0) ? 7 : 0):
            case ((1/(-0f % -1f) == Float.NEGATIVE_INFINITY) ? 8 : 0):
        }
    }
}
