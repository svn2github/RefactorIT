
import java.sql.*;

public class Test {
  private boolean Debug = true;
  
  public static String test() {
    ResultSet rs = null;
    Statement stmt = null;
    String sqlStatementInError, sqlStepTwo6 = "";
    
   /*]*/if(rs.next()){//If the sqlStepTwo4 record exists
     sqlStatementInError = sqlStepTwo6;
     rs = stmt.executeQuery(sqlStepTwo6);//Insert
   }else{//If the sqlStepTwo4 record does not exist
     if(Debug){
       System.out.println("Do error logging for stepTwo here");
     }
   }/*[*/
 }
}
