
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;


class Steuerung {
  public static final Color FilenameClippedColor = null;
  public static final Color DirnameColor = null;
  public static final Color FilenameColor = null;
  public static final Object ClipKategorie = null;
}


class FSV {
  public String getSystemDisplayName(File file) {
    return null;
  }

  public Icon getSystemIcon(File file) {
    return null;
  }
}


class Clips {
  public boolean contains(Object obj, File file) {
    return true;
  }
}


public class Test {

  private static FSV fsv = new FSV();
  private static Clips clips = new Clips();


  public class MyRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(
        JTable table, Object file,
        boolean isSelected, boolean hasFocus,
        int row, int column) {
      super.getTableCellRendererComponent(table, file, isSelected, hasFocus, row, column);
      File f = (File) file;
      this.setText(fsv.getSystemDisplayName(f));
      this.setIcon(fsv.getSystemIcon(f));
      Color c = 
     /*]*/clips.contains(Steuerung.ClipKategorie, f)
          ? Steuerung.FilenameClippedColor
          : (f.isDirectory()
          ? Steuerung.DirnameColor
          : Steuerung.FilenameColor);/*[*/
      this.setForeground(c);
      return this;
    }
  }
}
