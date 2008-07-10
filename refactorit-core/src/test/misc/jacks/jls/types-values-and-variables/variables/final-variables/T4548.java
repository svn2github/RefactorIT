
class T4548 {
    final Object[] objarr = {new Object(), new Object()};

    void swap() {
        Object tmp = objarr[0];
        objarr[0] = objarr[1];
        objarr[1] = tmp;
    }
}
    