package innerClassImportUsages;



/**
 Happens when you click MinimizeAccess on class name

 * <p>Title: </p>
 * <p>Description: BackupManagerUtils</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

/**
 * @violations 2
 */
public class BackupManagerUtil {
  private static final int INITIAL_BUFF_SIZE = 128*1024;

  private BackupManagerUtil() {
  }

  public static class SourceHeader {
   public void SourceHeader() {
   }
   String field;

  }


}
