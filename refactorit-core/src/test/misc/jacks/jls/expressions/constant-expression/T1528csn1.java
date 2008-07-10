
class T1528csn1 {
    
        final long l1 = 1L;
        final long l2 = Long.MAX_VALUE;
        void foo (int j) {
            switch (j) {
                case (int) l1:
                case (int) l2:
            }
        }
    
}
