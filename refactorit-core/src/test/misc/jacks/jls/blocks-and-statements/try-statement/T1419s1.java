
class T1419s1 {
    
        int i;
        void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    
}
