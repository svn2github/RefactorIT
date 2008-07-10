
abstract class DoubleInheritAbstractClassError_Superclass1 {
    abstract void foo();
}

abstract class DoubleInheritAbstractClassError_Superclass2 extends
    DoubleInheritAbstractClassError_Superclass1 {}

public class DoubleInheritAbstractClassError extends DoubleInheritAbstractClassError_Superclass2 {}
