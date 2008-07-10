
class T1419s3 {
    
        static int i;
        static void foo() {
            try {
                i = 1;
                throw new Exception();
            } catch (Exception i) {
            }
        }
    
}
