
class T8323lfif2 {
    
        interface I {
            int i = new Object(){ int bar() { return j; } }.bar();
            // not in declaring class
            int j = 1;
        }
    
}
