
class T1420catch11 {
    
        void choke() throws Exception {
            throw new java.io.IOException();
        }
        void foo() throws Exception {
            try {
                choke();
            } catch (java.io.IOException io) {
                // reachable, as choke() can throw any subclass
            }
        }
    
}
