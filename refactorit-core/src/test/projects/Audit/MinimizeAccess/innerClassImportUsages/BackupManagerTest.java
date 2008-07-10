package innerClassImportUsages.bug2050;

import innerClassImportUsages.bug2050.BackupManagerUtil.SourceHeader;

public class BackupManagerTest {
  /**
   * @audit MinimizeAccessViolation
   */
  public BackupManagerTest() {
  }
}
