class A {}

class B extends A {}

class C extends B {}


// Cyclic inheritance D->E->F->D->...
class D extends E {}
class E extends F {}
class F extends D {}