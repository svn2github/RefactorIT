
interface InterfaceSignatureConflict_Inter {
    void foo();
}

public abstract class InterfaceSignatureConflict implements InterfaceSignatureConflict_Inter {
    public abstract int foo();
}
