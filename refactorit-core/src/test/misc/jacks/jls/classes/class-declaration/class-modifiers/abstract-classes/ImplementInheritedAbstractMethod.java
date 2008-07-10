
abstract class ImplementInheritedAbstractMethod_Superclass1 {
    abstract void foo();
}

abstract class ImplementInheritedAbstractMethod_Superclass2 extends
    ImplementInheritedAbstractMethod_Superclass1 {}

public class ImplementInheritedAbstractMethod
    extends ImplementInheritedAbstractMethod_Superclass2 {
    void foo() {}
}
