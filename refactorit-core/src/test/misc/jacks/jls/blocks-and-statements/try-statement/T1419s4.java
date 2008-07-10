
class T1419s4 {
    
        void foo(final int i) {
            new Object() {
                {
                    try {
                        int j = i;
                        throw new Exception();
                    } catch (Exception i) {
                    }
                }
            };
        }
    
}
