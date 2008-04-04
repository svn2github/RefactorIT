/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.notused;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.EjbUtil;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.options.profile.OptionsPanel;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.utils.AuditProfileUtils;

import org.w3c.dom.Element;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Defines a not-used filter exclude rule.
 *
 * @author Tanel Alumae
 */
public abstract class ExcludeFilterRule {
  static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(ExcludeFilterRule.class);

  private static final String OPTION_PREFIX = "exclude.filter.";

  public static final ExcludeFilterRule[] ALL_RULES = new ExcludeFilterRule[] {
      new AccessorRule(),
      new InterfaceUsageRule(),
      new OverridingRule(),
      new PublicRule(),
      new ProtectedRule(),
      new UsedTagRule(),
      new ServletRule(),
      new EjbRule(),
      new JUnitRule(),
      new ExternalizableRule(),
      new WordsRule()
  };

  public static ExcludeFilterRule[] getDefaultRules() {
    List result = new ArrayList();
    for (int i = 0; i < ALL_RULES.length; i++) {
      if (ALL_RULES[i].isDefaultChecked()) {
        result.add(ALL_RULES[i]);
      }
    }
    return (ExcludeFilterRule[]) result.toArray(new ExcludeFilterRule[0]);
  }

  private final String key;
  private final String name;
  private final String description;
  protected Element configuration;

  public ExcludeFilterRule(final String key) {
    this.key = key;

    String prefix = "exclude." + key;

    name = resLocalizedStrings.getString(prefix + ".name");
    description = resLocalizedStrings.getString(prefix + ".description");
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  /**
   * Contents of this string must be placed inside HTML body.
   *
   * @return  HTML-styled description
   */
  public String getDescription() {
    return description;
  }

  public String toString() {
    return getName();
  }

  public abstract boolean isToBeExcluded(final BinMember member);

  public boolean isDefaultChecked() {
    return false;
  }

  public static boolean isDefaultSelected(String key) {
    for(int i=0; i<ALL_RULES.length; i++){
      if(ALL_RULES[i].getKey().equals(key)){
        return ALL_RULES[i].isDefaultChecked();
      }
    }
    return false;
  }

  /**
   * Returns a list of methods that are excluded from not-used list
   * as a result of the existance of the given method :)
   *
   * @param method
   * @return
   */
  public List getCustomDependants(BinMethod method) {
    return Collections.EMPTY_LIST;
  }

  public JComponent getPropertyEditor() {
    return null;
  }

  public void setProperties(JComponent propertyEditor) {

  }

  protected void setOptions(Element configuration) {
    this.configuration = configuration;
  }

  public void setProfile(Profile p) {
  }

  /**
   *
   * @return Map of String/String (option name and option value)
   */
  public MultiValueMap getOptions() {
    return new MultiValueMap(0);
  }


  //------------- rule implementations follow ------------------------

  public static class InterfaceUsageRule extends ExcludeFilterRule {

    public InterfaceUsageRule() {
      super("interface_usage");
    }

    public boolean isToBeExcluded(BinMember member) {
      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }

    public List getCustomDependants(BinMethod method) {
      List result = new ArrayList();
      addOverrides(method, result);
      return result;
    }

    /**
     * Adds metod and all it's direct and indirect overriding methods to the
     * list.
     *
     * @param method the method
     * @param result the list that is filled with the method and the overriding methods
     */
    private void addOverrides(BinMethod method, List result) {
      List overridesList = method.findOverrides();

      for (int i = 0, max = overridesList.size(); i < max; i++) {
        BinMethod overrideMethod = (BinMethod) overridesList.get(i);
        result.add(overrideMethod);
        addOverrides(overrideMethod, result);
      }
    }

  }


  public static class OverridingRule extends ExcludeFilterRule {

    public OverridingRule() {
      super("overriding");
    }

    public boolean isToBeExcluded(BinMember member) {
      if (member instanceof BinMethod) {
        final BinMethod method = (BinMethod) member;
        return !method.findOverrides().isEmpty();
      }
      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }

  public static class EmptyRule extends ExcludeFilterRule {
    public EmptyRule() {
      super("empty");
    }

    public boolean isToBeExcluded(BinMember member){
      return false;
    }
  }

  public static class PublicRule extends ExcludeFilterRule {

    public PublicRule() {
      super("public");
    }

    public boolean isToBeExcluded(BinMember member) {
      return member.isPublic();
    }
  }

  public static class ExternalizableRule extends ExcludeFilterRule {

    public ExternalizableRule() {
      super("externalizable");
    }

    public boolean isToBeExcluded(BinMember member) {
      return member instanceof BinConstructor
          && member.isPublic()
          && member.getOwner().isDerivedFrom(
              member.getProject().getTypeRefForName("java.io.Externalizable"));
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }


  public static class ProtectedRule extends ExcludeFilterRule {

    public ProtectedRule() {
      super("protected");
    }

    public boolean isToBeExcluded(BinMember member) {
      return member.isProtected();
    }
  }


  public static class AccessorRule extends ExcludeFilterRule {
    private static final String MAPPED = "mapped";
    private static final String INDEXED = "indexed";

    private boolean mappedAccessors = true;
    private boolean indexedAccessors = true;
    private JComponent editor;

    public AccessorRule() {
      super("accessor");
      mappedAccessors = Boolean.valueOf(GlobalOptions.getOption(OPTION_PREFIX + getKey()
          + "." + MAPPED, "true")).booleanValue();
      indexedAccessors = Boolean.valueOf(GlobalOptions.getOption(OPTION_PREFIX + getKey()
          + "." + INDEXED, "true")).booleanValue();
    }

    public boolean isDefaultChecked() {
      return true;
    }

    public boolean isToBeExcluded(BinMember member) {
      if (member instanceof BinMethod) {
        BinMethod method = (BinMethod) member;
        if (isGetter(method) || isSetter(method)) {
          return true;
        }
      }
      return false;
    }

    private boolean isSetter(BinMethod method) {
      String name = method.getName();
      if (!method.isStatic() && name.startsWith("set")
          && (name.length() > "set".length())
          && (Character.isUpperCase(name.charAt(3)))) {
        if (method.getReturnType().equals(BinPrimitiveType.VOID.getTypeRef())) {
          BinParameter[] parameters = method.getParameters();
          if ((parameters.length == 1)
              || ((parameters.length == 2)
              && (indexedAccessors
              && (parameters[0].getTypeRef().equals(BinPrimitiveType.INT))
              || (mappedAccessors
              && parameters[0].getTypeRef().equals(method.getProject().
              getTypeRefForName("java.lang.String")))))) {

            return true;
          }
        }
      }
      return false;
    }

    private boolean isGetter(BinMethod method) {
      String name = method.getName();
      if (!method.isStatic() && ((name.startsWith("get")
          && (name.length() > "get".length())
          && (Character.isUpperCase(name.charAt(3)))) ||
          (name.startsWith("is") && (name.length() > "is".length())
          && (Character.isUpperCase(name.charAt(2)))))) {
        if (!method.getReturnType().equals(BinPrimitiveType.VOID.getTypeRef())) {
          BinParameter[] parameters = method.getParameters();
          if ((parameters.length == 0)
              || ((parameters.length == 1)
              && (indexedAccessors
              && (parameters[0].getTypeRef().equals(BinPrimitiveType.INT))
              || (mappedAccessors
              && parameters[0].getTypeRef().equals(method.getProject().
              getTypeRefForName("java.lang.String")))))) {

            return true;
          }
        }
      }
      return false;
    }

    public JComponent getPropertyEditor() {
      if(editor == null) {
        editor = new EditorPanel(this, indexedAccessors, mappedAccessors);
      }
      return editor;
    }

    public void setProfile(Profile p){
      ((OptionsPanel)editor).setProfile(p);
    }

    public void setProperties(JComponent editor) {
      saveOptions();
    }

    public void saveOptions() {
      indexedAccessors = ((EditorPanel) editor).isIndexedChecked();
      mappedAccessors = ((EditorPanel) editor).isMappedChecked();
      if(configuration != null){
        configuration.setAttribute(INDEXED,Boolean.toString(indexedAccessors));
        configuration.setAttribute(MAPPED,Boolean.toString(mappedAccessors));
      } else {
        GlobalOptions.setOption(OPTION_PREFIX + getKey() + "." + INDEXED, "" + indexedAccessors);
        GlobalOptions.setOption(OPTION_PREFIX + getKey() + "." + MAPPED, "" + mappedAccessors);
      }
    }

    public MultiValueMap getOptions() {
      MultiValueMap map = new MultiValueMap(2);
      map.put("indexed-accessors", Boolean.toString(indexedAccessors));
      map.put("mapped-accessors", Boolean.toString(mappedAccessors));
      return map;
    }

    private static class EditorPanel extends JPanel implements OptionsPanel {
      private JCheckBox indexedCheckBox = new JCheckBox("Indexed accessors", true);
      private JCheckBox mappedCheckBox = new JCheckBox("Mapped accessors", true);
      private final AccessorRule rule;

      public EditorPanel(final AccessorRule rule,
          boolean indexedAccessors, boolean mappedAccessors) {
        this.rule = rule;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        updateOptions();
        add(indexedCheckBox);
        add(mappedCheckBox);

        ActionListener l = new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            rule.saveOptions();
          }
        };
        indexedCheckBox.addActionListener(l);
        mappedCheckBox.addActionListener(l);
      }

      public void updateOptions() {
        indexedCheckBox.setSelected(rule.indexedAccessors);
        mappedCheckBox.setSelected(rule.mappedAccessors);
      }

      public boolean isIndexedChecked() {
        return indexedCheckBox.isSelected();
      }

      public boolean isMappedChecked() {
        return mappedCheckBox.isSelected();
      }

      public void setProfile(Profile p) {
        if(p!=null && !p.isDefault()) {
          rule.configuration = p.getAuditItem(rule.getKey());
          rule.mappedAccessors = AuditProfileUtils.getBooleanAttributeOption(rule.configuration,
              MAPPED, true);
          rule.indexedAccessors = AuditProfileUtils.getBooleanAttributeOption(rule.configuration,
              INDEXED, true);
        } else {
          rule.mappedAccessors = Boolean.valueOf(GlobalOptions.getOption(OPTION_PREFIX + rule.getKey()
              + "." + MAPPED, "true")).booleanValue();
          rule.indexedAccessors = Boolean.valueOf(GlobalOptions.getOption(OPTION_PREFIX + rule.getKey()
              + "." + INDEXED, "true")).booleanValue();
        }
        updateOptions();
      }
      public void setTreeNode(CheckTreeNode treeNode){}
      public CheckTreeNode getTreeNode(){
        return null;
      }
    }
  }


  public static class UsedTagRule extends ExcludeFilterRule {

    private BinTypeRef cachedOwner = null;
    private boolean cachedResult = false;

    public UsedTagRule() {
      super("used_tag");
    }

    public boolean isToBeExcluded(BinMember member) {
      if (isMarkedAsUsed(member)) {
        return true;
      }
      if (isOwnerMarkedAsUsed(member)) {
        return true;
      }
      return false;
    }

    private boolean isOwnerMarkedAsUsed(BinMember member) {
      BinTypeRef owner = member.getOwner();
      if (owner == null) {
        return false;
      }
      if (!owner.equals(cachedOwner)) {
        cachedOwner = owner;
        if (owner != null) {
          cachedResult = isMarkedAsUsed(owner.getBinCIType());
        } else {
          cachedResult = false;
        }
      }
      return cachedResult;
    }

    /**
     * Determines whether the member or type is marked as used.
     */
    private boolean isMarkedAsUsed(final LocationAware locationAware) {
      final JavadocComment jdoc = Comment.findJavadocFor(locationAware);
      if (jdoc != null && jdoc.getText().toLowerCase().indexOf("@used") != -1) {
        return true;
      }
      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }


  public static class EjbRule extends ExcludeFilterRule {

    public EjbRule() {
      super("ejb");
    }

    public boolean isToBeExcluded(BinMember member) {
      if (member instanceof BinMethod) {
        final BinMethod method = (BinMethod) member;
        if (EjbUtil.isEjbMethod(method)) {
          return true;
        }
      } else if (member instanceof BinCIType) {
        return EjbUtil.isEnterpriseBean(((BinCIType) member).getTypeRef());
      }

      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }


  public static class ServletRule extends ExcludeFilterRule {

    public ServletRule() {
      super("servlet");
    }

    public boolean isToBeExcluded(BinMember member) {
      if (member instanceof BinMethod) {
        final BinMethod method = (BinMethod) member;
        if (EjbUtil.isServletMethod(method)) {
          return true;
        }
      } else if (member instanceof BinCIType) {
        return EjbUtil.isServlet(((BinCIType) member).getTypeRef());
      }
      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }


  public static class JUnitRule extends ExcludeFilterRule {

    public JUnitRule() {
      super("junit");
    }

    public boolean isToBeExcluded(BinMember member) {
      if (member instanceof BinCIType) {
        if (isTestCase(((BinCIType) member).getTypeRef())) {
          return true;
        }
      } else if (member instanceof BinMethod) {
        if (isTestMethod((BinMethod) member)) {
          return true;
        }

      }
      return false;
    }

    private boolean isTestCase(BinTypeRef type) {
      for (BinTypeRef testableSuperClass = type; testableSuperClass != null;
          testableSuperClass = testableSuperClass.getSuperclass()) {
        if (testableSuperClass.getQualifiedName().equals(
            "junit.framework.TestCase")) {
          return true;
        }
      }
      return false;
    }

    private boolean isTestMethod(BinMethod method) {
      // check for 'public Test suite() {..}'
      if (method.isPublic() && method.getName().equals("suite")
          && (method.getParameters().length == 0)) {
        return "junit.framework.Test".equals(method.getReturnType().
            getQualifiedName());
      }
      if (isTestCase(method.getOwner())) {
        if (method.isPublic() && (method.getName().startsWith("test")
            || method.getName().equals("setUp")
            || method.getName().equals("tearDown"))
            || method instanceof BinConstructor) {
          return true;
        }
      }
      return false;
    }

    public boolean isDefaultChecked() {
      return true;
    }
  }


  public static class WordsRule extends ExcludeFilterRule  {
    private List words;
    private JComponent editor;

    public WordsRule() {
      super("words");
      words = StringUtil.deserializeStringList(GlobalOptions.getOption(OPTION_PREFIX
          + getKey() + ".wordlist", ""));
    }

    public boolean isToBeExcluded(BinMember member) {
      String memberName = member.getName();
/*      if (memberName.matches(".*Page") || memberName.matches(".*Test")
          || memberName.matches(".*RemoteHome")
          || memberName.matches(".*Remote")
          || memberName.matches(".*Local")
          || memberName.matches("exec.*")) {
        return true;
      }*/
      if (words.contains(memberName)) {
        return true;
      } else {
        return false;
      }
    }

    public JComponent getPropertyEditor() {
      if(editor == null) {
        editor = new EditorPanel(this, words);
      }
      return editor;
    }

    public void setProperties(JComponent editor) {
      words = ((EditorPanel) editor).getWordList();
      saveOptions();
    }

    public void setOptions(Element configuration) {
      super.setOptions(configuration);
      words = StringUtil.deserializeStringList(configuration.getAttribute("wordlist"));
      ((EditorPanel)editor).buildModel();
    }

    public void setProfile(Profile p){
      ((OptionsPanel)editor).setProfile(p);
    }

    public MultiValueMap getOptions() {
      MultiValueMap map = new MultiValueMap(words.size());
      for(Iterator it = words.iterator(); it.hasNext(); ) {
        String str = (String)it.next();
        map.put("exclude-word", str);
      }
      return map;
    }

    public boolean isDefaultChecked() {
      return true;
    }

    public void saveOptions() {
      if(configuration != null){
        configuration.setAttribute("wordlist",StringUtil.serializeStringList(words));
        configuration.getElementsByTagName("");
      } else {
        GlobalOptions.setOption(OPTION_PREFIX + getKey() + ".wordlist", StringUtil.serializeStringList(words));
      }
    }

    private static class EditorPanel extends JPanel implements OptionsPanel {
      JTextField word = new JTextField();
      JList list = new JList();
      final WordsRule rule;

      private JButton add = new JButton(
          resLocalizedStrings.getString("button.add"));
      private JButton remove = new JButton(
          resLocalizedStrings.getString("button.remove"));

      public EditorPanel(final WordsRule rule, List wordList) {
        super(new GridBagLayout());
        this.rule = rule;

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 5, 0, 5);
        add(word, constraints);

        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(5, 0, 0, 5);
        add(add, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(new JScrollPane(list), constraints);

        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(5, 0, 0, 5);
        add(remove, constraints);

        buildModel();
        list.getSelectionModel().setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        add.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String str = word.getText().trim();
            if (str.length() > 0) {
              ((DefaultListModel) list.getModel()).addElement(str);
              saveList();
              word.setText("");
            }
          }
        });
        remove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (list.isSelectionEmpty()) {
              return;
            }
            int index = list.getSelectedIndex();
            ((DefaultListModel) list.getModel()).remove(index);
            saveList();
          }
        });
      }

      public void saveList() {
        rule.words = getWordList();
        rule.saveOptions();
        buildModel();
      }

      public void buildModel() {
        DefaultListModel model = new DefaultListModel();
        for (Iterator i = rule.words.iterator(); i.hasNext(); ) {
          model.addElement(i.next());
        }
        list.setModel(model);
      }

      public List getWordList() {
        return Arrays.asList(((DefaultListModel) list.getModel()).toArray());
      }

      public void setProfile(Profile p){

        if(!p.isDefault()) {
          rule.configuration = p.getAuditItem(rule.getKey());
          rule.words = StringUtil.deserializeStringList(rule.configuration.getAttribute("wordlist"));
        } else {
          rule.words = StringUtil.deserializeStringList(GlobalOptions.getOption(OPTION_PREFIX
              + rule.getKey() + ".wordlist", ""));
          rule.configuration = null;
        }

        buildModel();

      }

      public void setTreeNode(CheckTreeNode treeNode){}
      public CheckTreeNode getTreeNode(){
        return null;
      }
    }

  }
}
