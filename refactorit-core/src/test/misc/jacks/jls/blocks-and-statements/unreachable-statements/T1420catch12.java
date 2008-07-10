
class T1420catch12 {
    
        void foo() throws Exception {
            try {
                throw new Exception();
            } catch (java.io.IOException io) {
                // unreachable, as new cannot create subclass, and
                // the constructor has no throws clause
            }
        }
    
}
