package Forin;
import java.util.*;

public class A{
  int xx = 0;
  int[]x = {1,2,3,4,5};
  static String[] arr={"one","two","trhee"};
  
  /**
   * @audit ForinForArrViolation
   */
  void arrTraversalOnField(){
    //normal work
    for(int i=0;i<x.length;i++){ 
        System.out.println("blah"+x[i]);  
    } 
  }

  /**
   * @audit ForinForArrViolation
   */
  void arrTraversalOnLocalVariable() {
    String strArr[]={"one","two","three"};//{1,2,3};
    for(int i=0;i<strArr.length;i++){ 
        System.out.println("blah"+strArr[i]);  
    } 
  }

  /**
   * @audit ForinForArrViolation
   */
  void arrTraversalOnTypeDotField(){
    for(int i=0;i<A.arr.length;i++){
        System.out.println(A.arr[i]);
    }
  }
  
  /**
   * @audit ForinForIteratorViolation
   */
  void forIterator() {
    List list=Arrays.asList(arr);
    for(Iterator i=list.iterator();i.hasNext()  /*blah blah*/   ;/* ah ah*/)
    {
        Object obj=i.next();
        System.out.println(""+obj.toString());
        System.out.println(""+obj.toString());
    }
  }
  
  /**
   * @audit ForinWhileIteratorViolation
   */
  void whileIterator() {
    List list=Arrays.asList(arr);
    System.out.println("blah");
    Iterator i=list.iterator();
    while(i.hasNext()  /*blah blah*/   /* ah ah*/)
    {
        Object obj=i.next();
        System.out.println(""+obj.toString());
        System.out.println(""+obj.toString());
    }
  }
  //shouldn't detect anything
  /**
   * 
   */
  void nonForinCandidates(){
    for(int i=0;i<x.length;i++){
        x[i]=i;
        System.out.println("blah"+i);
    }
    for(int i=0;i<x.length;i++){
        x[i]=10;
    }
    for(int i=0;i<x.length;i++){
        System.out.println("blah"+i);
    } 
    
    for(int i:x){
        System.out.println(i);
    }
  }
}