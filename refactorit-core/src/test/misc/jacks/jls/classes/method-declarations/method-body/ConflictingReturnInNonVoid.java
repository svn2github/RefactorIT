
public class ConflictingReturnInNonVoid {
    public boolean foo() {
        int i = 0;
        if (i == 1) {
            return true;
        } else {
            return;
        }
    }
}
