
class T511r5 {
    
        Object[] oa1 = {new Object[1]};
        Object[] oa2 = (Object[]) null;
        Object[] oa3 = (Object[]) oa1;
        Object[] oa4 = (Object[]) oa2;
        Object[] foo() { return new Object[0]; }
        Object[] oa5 = (Object[]) foo();
    
}
