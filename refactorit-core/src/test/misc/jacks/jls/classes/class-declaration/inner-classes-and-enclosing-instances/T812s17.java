
class T812s17 {
    
        T812s17(Object o) {}
        T812s17() {
            // static context in explicit constructor invocation
            this(new Object() {
                static int i;
            });
        }
    
}
