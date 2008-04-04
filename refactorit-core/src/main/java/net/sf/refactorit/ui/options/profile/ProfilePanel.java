/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.checktree.JCheckTree;
import net.sf.refactorit.ui.dialog.RitDialog;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;



/**
 * @author Igor Malinin
 * @author Risto
 * @author Anton Safonov
 */
public final class ProfilePanel extends JPanel implements IProfilePanel {
  static final String PROFILE_EXTENSION = ".profile";

  private static final String DEFAULT_PROFILE = "<default>";

  static class ComboBoxRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
        JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus
        ) {
      if (value instanceof File) {
        File file = (File) value;

        if (index == -1) {
          value = getShortName(file);
        } else {
          try {
            value = (index + 1) + ". " + file.getCanonicalPath();
          } catch (IOException e) {
            value = (index + 1) + ". " + getShortName(file);
          }
        }
      }

      return super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
    }

    private static String getShortName(File file) {
      String name = file.getName();
      if (name.endsWith(PROFILE_EXTENSION)) {
        name = name.substring(0, name.length() - PROFILE_EXTENSION.length());
      }
      return name;
    }
  }

  public abstract static class TreeNode extends CheckTreeNode {
    public TreeNode(final UserObject userObject) {
      super(userObject);
    }

    public boolean isSelected() {
      return Boolean.valueOf(getItem(getKey()).getAttribute("active")).
          booleanValue();
    }

    public void setSelected(boolean selected) {
      getItem(getKey()).setAttribute("active", selected ? "true" : "false");
    }

    protected String getKey() {
      return ((UserObject) getUserObject()).getKey();
    }

    protected abstract Element getItem(String key);
  }


  static class EmptyOptionsPanel extends JPanel implements OptionsPanel {
  	public void setProfile(Profile profile) {
    }
    
    public void setTreeNode(CheckTreeNode treeNode) {
    }
    
    public CheckTreeNode getTreeNode(){
      return null;
    }
  }

  CardLayout optionsLayout = new CardLayout();
  JPanel optionsPanel = new JPanel(optionsLayout);

  Profile profile;

  JEditorPane editor;

  DefaultComboBoxModel profilesModel;

  private DefaultTreeModel treeModel;

  private final ProfileType profileType;

  public ProfilePanel(ProfileType c) {
    this(c, c.createDefaultProfile());
  }

  public ProfilePanel(ProfileType c, Profile p) {
    super(new BorderLayout());

    this.profileType = c;
    this.profile = p;

    c.setProfilePanel(this);

    JPanel right = new JPanel(new GridLayout(2, 1));
    right.add(createDescriptionPanel());
    right.add(createOptionsPanel());

    JPanel center = new JPanel(new GridLayout(1, 2));

    add(createProfilePanel(), BorderLayout.NORTH);
    add(center);
    JComponent left = createTreePanel();
    center.add(left);
    center.add(right);
    left.setNextFocusableComponent(optionsPanel);
  }

  public Profile getProfile() {
    return profile;
  }

  public void saveCurrentProfile() {
    profile.save();
  }

  public Component[] getOptionsPanelComponents(){
    return optionsPanel.getComponents();
  }
  
  private JComponent createProfilePanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Profile"));

	panel.add(new JLabel("Name:"));

    profilesModel = new DefaultComboBoxModel();

    String last = GlobalOptions.getOption(profileType.getParametersPrefix()
        + "latest");
    Object select = DEFAULT_PROFILE;
    for (int i = 0; i < 9; i++) {
      String name = GlobalOptions.getOption(profileType.getParametersPrefix() +
          "recent." + i);
      if (name == null) {
        continue;
      }

      if (name.length() > 0) {
        File file = new File(name);

        if (!file.exists() || !file.isFile() ||
            profilesModel.getIndexOf(file) >= 0) {
          continue; // not interested - obsolete
        }

        profilesModel.addElement(file);
        
        if (profile != null && name.equals(profile.getFileName()) ||
            (name.equals(last))) {
            select = file;
        }
      }
    }

    // last element is default
    profilesModel.addElement(DEFAULT_PROFILE);
    profilesModel.setSelectedItem(select);

    final JComboBox comboBox = new JComboBox(profilesModel) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if (dim.width < 400) {
          dim.width = 400;
        }
        return dim;
      }
    };
    comboBox.setRenderer(new ComboBoxRenderer());
    comboBox.setMinimumSize(new Dimension(200, 16));
    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selected = profilesModel.getSelectedItem();
        File file = (selected instanceof File)? (File)selected: null;
        try {
          if(e!=null){
            profile = null;
          }
          openProfile(file);
        } catch (Exception x) {
          x.printStackTrace();
        }
      }
    };
    
    listener.actionPerformed(null); // let's load selected profile and update the tree
    
    comboBox.addActionListener(listener);
    panel.add(comboBox);

    final JFileChooser chooser = new JFileChooser();

    JButton save = new JButton("Save As...");
    save.setDefaultCapable(false);
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selected = profilesModel.getSelectedItem();
        if (selected != null && selected instanceof File) {
          chooser.setCurrentDirectory(((File) selected).getParentFile());
        } else {
          chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());
        }

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());

        File file = chooser.getSelectedFile();
        if (file != null) {
          String str = file.getName();
          if (str.indexOf('.') < 0) {
            file = new File(file.getPath() + PROFILE_EXTENSION);
          }

          try {
            profile.serialize(file);
            openProfile(file);
          } catch (Exception x) {
            x.printStackTrace();
          }
        }
      }
    });
    panel.add(save);

    JButton open = new JButton("Open...");
    open.setDefaultCapable(false);
    open.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());

        File file = chooser.getSelectedFile();
        if (file != null) {
          try {
            openProfile(file);
          } catch (Exception x) {
            x.printStackTrace();
          }
        }
      }
    });
    panel.add(open);

    return panel;
  }


  
  void openProfile(File file) throws IOException, SAXException {
    if (file == null) {
      if(profile == null){
        profile = profileType.createDefaultProfile();
      }
    } else {
      String str = file.getName();
      if (str.indexOf('.') < 0) {
        file = new File(file.getPath() + PROFILE_EXTENSION);
      }

      profile = new Profile(file);

      if(profile.isOldVersion()){
          profile.upgradeToNewVersion();
          profile.serialize(file);
      }
    }

    if (treeModel != null){
      Enumeration e = ((CheckTreeNode) treeModel.getRoot()).preorderEnumeration();
			//profileType.createNodes((CheckTreeNode) treeModel.getRoot());
      while (e.hasMoreElements()) {
        treeModel.nodeChanged((CheckTreeNode) e.nextElement());
      }
			JCheckTree.updateSelections((CheckTreeNode) treeModel.getRoot());
		}
    
    Component[] comps = optionsPanel.getComponents();
    
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof OptionsPanel) {
        ((OptionsPanel) comps[i]).setProfile(this.profile);
      } else {
        System.err.println("strange component: " + comps[i]);
      }
    }

    synchronized (profilesModel) {
      if (file != null) {
        int index = profilesModel.getIndexOf(file);
        if (index >= 0) {
          profilesModel.removeElementAt(index);
        }
        profilesModel.insertElementAt(file, 0);
        int length = profilesModel.getSize();
        if (length > 10) {
          profilesModel.removeElementAt(length - 2); // length-1 is DEFAULT
        }
        // FIXME: causes opening current profile and saving options twice
        profilesModel.setSelectedItem(file);

        GlobalOptions.setOption(profileType.getParametersPrefix() + "latest",
            file.getAbsolutePath());
      } else {
        GlobalOptions.setOption(profileType.getParametersPrefix() + "latest",
            "");
      }

      int length = profilesModel.getSize();
      for (int i = 0; i < length - 1; i++) {
        file = (File) profilesModel.getElementAt(i);
        GlobalOptions.setOption(profileType.getParametersPrefix() +
            "recent." + i, file.getAbsolutePath());
      }
    }
    GlobalOptions.save();
  }

  private JComponent createTreePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(profileType.getName()));

    treeModel = new DefaultTreeModel(createTreeNodes());

    JCheckTree tree = new JCheckTree(treeModel);
		JCheckTree.updateSelections((CheckTreeNode) treeModel.getRoot());
    tree.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

    tree.expandAll();
//    for (int i = tree.getRowCount() - 1; i > 0;) {
//      tree.expandRow(--i);
//    }
    tree.optionsChanged();

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e){
        // reset to empty
        editor.setText("");
        optionsLayout.show(optionsPanel, "");

        TreePath path = e.getNewLeadSelectionPath();
        if (path != null) {
          Object obj = path.getLastPathComponent();
          if (obj instanceof CheckTreeNode) {
            CheckTreeNode node = (CheckTreeNode) obj;
						// set specific values if available
            if (obj instanceof TreeNode) {
              UserObject o = (UserObject) node.getUserObject();
              editor.setText(o.getDescription());
              optionsLayout.show(optionsPanel, o.getKey());
            } else {
              optionsLayout.show(optionsPanel, obj.toString());
            }
            
          }
        }

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            editor.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
          }
        });
      }
    });

    panel.add(new JScrollPane(tree));

    return panel;
  }

  private CheckTreeNode createTreeNodes() {
    CheckTreeNode root = new CheckTreeNode(profileType.getName(), true);

    profileType.createNodes(root);

    return root;
  }

  private JComponent createDescriptionPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Description"));

    editor = new JEditorPane();
    editor.setEditorKitForContentType("text/html", new HTMLEditorKit());
    editor.setContentType("text/html");
    editor.setEditable(false);

    HTMLDocument document = (HTMLDocument) editor.getDocument();
    document.setPreservesUnknownTags(false);

    panel.add(new JScrollPane(editor));

    return panel;
  }

  private JComponent createOptionsPanel() {
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

	// default card when nothing selected in the tree
    addOptionsPanel(new EmptyOptionsPanel(), "");

    return optionsPanel;
  }

  public ProfileType getProfileType() {
    return profileType;
  }

  public void addOptionsPanel(OptionsPanel p, String key) {
    optionsPanel.add((Component) p, key);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle("ProfilePanel - Test");
    frame.setSize(750, 550);

    frame.getContentPane().add(
        new ProfilePanel(new MetricsProfileType(), null));

    frame.show();
  }
}
