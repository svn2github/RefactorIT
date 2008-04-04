/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.ResourceBundle;


/**
 * @author Tonis Vaga
 * @author Anton Safonov
 */
public class JvmSelector extends JPanel {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(ProjectOptions.class);

  private final String automatic = resLocalizedStrings.getString("jvm.automatic");
  private final String jdk13 = resLocalizedStrings.getString("jvm.13");
  private final String jdk14 = resLocalizedStrings.getString("jvm.14");
  private final String jdk50 = resLocalizedStrings.getString("jvm.50");

  JComboBox box = new JComboBox(new String[] {automatic, jdk13, jdk14, jdk50});

  public JvmSelector(int defaultValue) {
    this.add(new JLabel(resLocalizedStrings.getString("jvm.mode")));
    this.add(box);

    switch (defaultValue) {
      case FastJavaLexer.JVM_AUTOMATIC:
        box.setSelectedItem(automatic);
        break;
      case FastJavaLexer.JVM_13:
        box.setSelectedItem(jdk13);
        break;
      case FastJavaLexer.JVM_14:
        box.setSelectedItem(jdk14);
        break;
      case FastJavaLexer.JVM_50:
      default:
        box.setSelectedItem(jdk50);
        break;
    }
  }

  /**
   * @return one of the FastJavaLexer.ASSERT_XXX constants;
   *    default is ASSERT_AUTOMATIC.
   */
  public int getAssertKeywordSupport() {
    String item = (String) box.getSelectedItem();
    if (item.equals(automatic)) {
      return FastJavaLexer.JVM_AUTOMATIC;
    }

    if (item.equals(jdk13)) {
      return FastJavaLexer.JVM_13;
    }

    if (item.equals(jdk14)) {
      return FastJavaLexer.JVM_14;
    }

    if (item.equals(jdk50)) {
      return FastJavaLexer.JVM_50;
    }

    throw new IllegalStateException("assert enabled= " + item);
  }
}
