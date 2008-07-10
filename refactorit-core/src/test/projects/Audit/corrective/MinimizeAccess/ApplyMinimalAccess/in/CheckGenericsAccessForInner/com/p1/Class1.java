package CheckGenericsAccessForInner.com.p1;

import java.util.*;

/**
 * @violations 11
 */
public class Class1 {
	
 private List<A.InnerA> fieldAList;
 private List<A.InnerB> fieldBList;
	
 private void drawA(List<? super A.InnerC> list) {
 }
 
 private void drawB(List<? extends A.InnerD> list) {
 }
 
 private void meth1() {
	fieldAList = new ArrayList<A.InnerA>();
 }
 
 private void meth2() {
	fieldBList = new ArrayList<A.InnerB>();
 }
 
 private void meth3() {
	Map<String, A.InnerE> map;
 }
 
 private void meth4() {
	new HashMap<Integer, A.InnerF>();
 }
 private void meth5(List<? extends Comparable<? super A.InnerG>> list) {
 }
 
 private void meth5a(List<? super Comparable<? extends A.InnerG>> list) {
 }
 
 private void meth6() {
	List<List<List<A.InnerI>>> list;
 }
 
 private void meth7(List<Map<List<Map<Integer,A.InnerJ>>,String>> list) {
 }
 
 private class Inner {
	public void meth() {
		List <A.InnerK> list;
	}
 }
}

class A {
 static class InnerA {
 }
 
 static class InnerAp {
 }
 
 static class InnerB {
 }
 
 static class InnerBp {
 }
 
 static class InnerC {
 }

 static class InnerCp {
 }
 
 static class InnerD {
 }
 
 static class InnerDp {
 }
 
 static class InnerE {
 }
 
 static class InnerEp {
 }
 
 static class InnerF {
 }
 
 static class InnerFp {
 }

 static class InnerG {
 }
 
 static class InnerGp {
 }
 
 static class InnerH {
 }
 
 static class InnerHp {
 }
 
 static class InnerI {
 }
 
 static class InnerIp {
 }
 
 static class InnerJ {
 }
 
 static class InnerJp {
 }
 
 static class InnerK {
 }
 
 static class InnerKp {
 }
}

interface I<J, K> {
  public boolean compare(J j, K k);
}

class B<L, M> {
}

class C <J, K, L, M, T extends Map<String, ? extends A.InnerH>> extends B<L, M> implements I<J, K> {
  public boolean compare(J j, K k) {
    return k.equals(j);
  }
}


