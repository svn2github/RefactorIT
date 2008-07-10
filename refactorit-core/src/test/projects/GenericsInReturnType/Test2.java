abstract class CallExpression<T,I> {
  public final T target() {
      return null;
  }

  public final I target2() {
      return null;
  }
}


class StaticCallExpression<T> extends CallExpression<T,String> {
}


class StaticCallExpression2 extends StaticCallExpression<Integer> {

}


class ByteCodeUtility {
  public void emitStaticInvoke(Integer str) {
      System.out.println ("Writing: " + str);
  }

  public void emitStaticInvoke2(String str) {
      System.out.println ("Writing: " + str);
  }
}

public class Test2 {
    public static void main(String[] args) {
        ByteCodeUtility bcu = new ByteCodeUtility();
        StaticCallExpression2 arg = new StaticCallExpression2();
        bcu.emitStaticInvoke(arg.target());
        bcu.emitStaticInvoke2(arg.target2());
        String str = arg.target2();
        Integer intg = arg.target();
    }
}

