package vararity_in
public class Test2 {
    
    /** Creates a new instance of Test2 */
    public Test2() {
    }
    
    public static void main(String... args){
        System.out.println("<main>");
        
        objectArrayTest(new Object[]{"stringOne","stringTwo","stringThree"});
        objectArrayTest((Object)new Object[]{"stringOne","stringTwo","stringThree"});
        objectArrayTest((Object[])new Object[]{"stringOne","stringTwo","stringThree"});
        
        Object arg=null;
        objectArrayTest(arg);
        System.out.println("</main>");
    }
    
    static void objectArrayTest(Object... a){
        String indent="  ";
        System.out.println(indent+"<objectArrTest>");
        if(a==null){
            System.out.println(indent+indent+"a is null:"+a);
            return;
        }
        System.out.println(indent+indent+"param a is of type:"+a.getClass().getComponentType());
        System.out.println(indent+indent+"a value is:"+Arrays.deepToString(a));
        System.out.println(indent+"</objectArrTest>");
    }
//    
//    static void objectArrayTest2(String s, Object... a){
//        System.out.println("<objectArrTest>");
//        System.out.println("param a is of type:"+a.getClass().getComponentType());
//        System.out.println("a value is:"+Arrays.asList(a));
//        System.out.println("</objectArrTest>");        
//    }
}