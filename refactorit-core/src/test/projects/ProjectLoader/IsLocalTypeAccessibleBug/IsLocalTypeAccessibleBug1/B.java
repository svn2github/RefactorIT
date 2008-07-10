import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class B {
  B() { }
  
  public void runResolution() {
    class QuestionDialog extends JDialog {
      
      QuestionDialog() {
        super((Dialog)null, "Move field?", true);
      }
      
      private JComponent createButtonsPanel() {
        final JButton buttonOK = new JButton("Yes");
        buttonOK.setDefaultCapable(true);
        buttonOK.setMnemonic(KeyEvent.VK_O);
        buttonOK.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();

            class Local {
              private JComponent createButtonsPanel() {
                buttonOK.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                    dispose();
                  }
                });
              
                return null;
              }
            }
          }
        });
        
        return null;
      }
    }
  }
}
