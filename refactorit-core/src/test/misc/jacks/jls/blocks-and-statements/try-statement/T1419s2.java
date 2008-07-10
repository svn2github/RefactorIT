
class T1419s2 {
    
        static int i;
        void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    
}
