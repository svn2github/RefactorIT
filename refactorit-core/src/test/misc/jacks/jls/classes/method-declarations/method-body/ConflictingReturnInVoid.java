
public class ConflictingReturnInVoid {
    public void foo() {
        int i = 0;
        if (i == 1) {
            return true;
        } else {
            return;
        }
    }
}
