/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.javadoc;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.common.util.Patch;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.format.BinFormatter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;


public class JavaHtmlDocParser {
  /**
   * @return root element of the document or null if failed to load/parse.
   */
  public String getJavadoc(final File docFile, final BinMember member) {
    
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206490 patching. 
    // may remove if that bug is fixed.
    try {
      new Patch(docFile).replace("<DT><PRE>", "<DT>     ");
    } catch (IOException e) {
      return null;
    }
    // ----
    
    final FileReader reader;
    try {
      reader = new FileReader(docFile);
    } catch (java.io.FileNotFoundException e) {
      return null; // hmm, we already checked for the file...
    }

    HTMLEditorKit editor = new HTMLEditorKit();

    final HTMLDocument doc = (HTMLDocument) editor.createDefaultDocument();
    doc.setPreservesUnknownTags(true);
    try {
      editor.read(reader, doc, 0);
    } catch (IOException e) {
      return null;
    } catch (javax.swing.text.BadLocationException e) {
      return null; // shouldn't get here, actually
    } catch (Exception ex) {
      ex.printStackTrace();
      return null; // just in case
    }

    return extractJavadocString(doc.getDefaultRootElement(), member, editor);
  }

  /**
   * @return javadoc for the given member without package and HTML and BODY tags
   */
  private String extractJavadocString(Element root, BinMember member,
      HTMLEditorKit editor) {
    int start = 0, end = 0;

    try {
      if (member instanceof BinCIType) {
        Element anchor = findElement(root,
            "comment", " ======== START OF CLASS DATA ======== ", true)
            .getParentElement();
        anchor = getNextElement(anchor);
        anchor = findElement(anchor, HTML.Tag.HR.toString(), null, true)
            .getParentElement();
        anchor = getNextElement(anchor);
        start = anchor.getStartOffset();

        anchor = findElement(anchor, HTML.Tag.HR.toString(), null, true)
            .getParentElement();
        end = anchor.getStartOffset() - 1;
      } else {
//if (member instanceof BinConstructor) {
//  dumpTree(root);
//}
        Element anchor = findElement(root, "name", getMemberName(member), true)
            .getParentElement();
        anchor = findElement(anchor, HTML.Tag.PRE.toString(), null, true);
        start = anchor.getStartOffset();

        anchor = findElement(anchor, HTML.Tag.DL.toString(), null, true);
        end = anchor.getEndOffset();
      }
    } catch (NullPointerException e) {
      return ""; // failed to analyze javadoc tree
    }

    String result = "";
    if (start != end) {
      try {
        StringWriter sw = new StringWriter(128);
        editor.write(sw, root.getDocument(), start, end - start);
        result = sw.toString();

        // crop automatically generated HTML and BODY tags
        int index = result.indexOf("<body>");
        if (index > 0) {
          result = result.substring(index + 6);
        }

        if (member instanceof BinCIType) {
          index = result.lastIndexOf("</body>");
          if (index > 0) {
            result = result.substring(0, index);
          }
        } else {
          // remove top DL also - it causes shifting of the whole text!
          index = result.lastIndexOf("</dl>");
          if (index > 0) {
            result = result.substring(0, index);
          }

          index = result.indexOf("<dl>");
          if (index > 0) {
            result = result.substring(0, index) + result.substring(index + 5);
          }
        }
        result = result.trim();
        int length = 0;
        while (length != result.length()) {
          length = result.length();
          result = StringUtil.replace(result, "  ", " ");
        }
        result = StringUtil.replace(result, " <p>\n \n </p>", "");
        result = StringUtil.replace(result, "\n </pre>\n", "</pre>");
        result = StringUtil.replace(result, "</blockquote>\n <p>\n",
            "</blockquote><p>");
      } catch (IOException e) {
        result = "<BR>IOException: " + e.getLocalizedMessage() + "<BR>";
      } catch (BadLocationException e) {
        result = "<BR><B>BadLocationException!!!</B><BR>";
      }
    }

//System.err.println("result: \"" + StringUtil.printableLinebreaks(result) + "\"");
    return result.trim();
  }

  private Element getNextElement(Element element) {
    HTMLDocument.AbstractElement parent
        = (HTMLDocument.AbstractElement) element.getParentElement();
    int cur = parent.getIndex((HTMLDocument.AbstractElement) element);
    return parent.getElement(cur + 1);
  }

  private String getMemberName(final BinMember member) {
    String memberName = member.getName();

    if (member instanceof BinMethod) {
      memberName += '(';
      BinParameter[] params = ((BinMethod) member).getParameters();
      for (int i = 0, max = params.length; i < max; i++) {
        if (i > 0) {
          memberName += ", ";
        }
        memberName += BinFormatter.formatQualified(params[i].getTypeRef());
      }
      memberName += ')';
    }

    return memberName;
  }

  private Element findElement(final Element startElement,
      final String name, final String value, boolean visitAdjacent) {
    // element self (check only when no value given, otherwise it was attribute search)
    if (value == null && name.equals(startElement.getName())) {
      return startElement;
    }

    // attributes
    if (checkAttributes(startElement.getAttributes(), name, value)) {
      return startElement;
    }

    // children
    for (int i = 0, max = startElement.getElementCount(); i < max; i++) {
      Element curEl = startElement.getElement(i);
      curEl = findElement(curEl, name, value, false);
      if (curEl != null) {
        return curEl;
      }
    }

    // neighbours
    if (visitAdjacent) {
      HTMLDocument.AbstractElement parent
          = (HTMLDocument.AbstractElement) startElement.getParentElement();
      if (parent != null) {
        int cur = parent
            .getIndex((HTMLDocument.AbstractElement) startElement);
        for (int i = cur + 1; i < parent.getElementCount(); i++) {
          Element curEl = parent.getElement(i);
          curEl = findElement(curEl, name, value, false);
          if (curEl != null) {
            return curEl;
          }
        }
      }
    }

    return null;
  }

  private boolean checkAttributes(final AttributeSet attrs,
      final String name, final String value) {
    Enumeration attrNames = attrs.getAttributeNames();
    while (attrNames.hasMoreElements()) {
      Object attrKey = attrNames.nextElement();
      Object attrValue = attrs.getAttribute(attrKey);
      if (attrValue instanceof AttributeSet) {
        if (checkAttributes((AttributeSet) attrValue, name, value)) {
          return true;
        }
      } else {
//System.err.println("attr: " + attrKey + " - " + attrValue);
        if (name.equals(attrKey.toString())) {
          if (value.equals(attrValue.toString())) {
            return true;
          }
        }
      }
    }

    return false;
  }

//  private void dumpTree(Element root) {
//    BinTree tree = new BinTree((AbstractDocument.AbstractElement) root) {
//      public String convertValueToText(Object value, boolean selected,
//          boolean expanded, boolean leaf, int row,
//          boolean hasFocus) {
//        if (value instanceof AbstractDocument.AbstractElement) {
//          String name = value.getClass().getName();
//          name = name.substring(name.lastIndexOf('.') + 1);
//          if (name.lastIndexOf('$') > 0) {
//            name = name.substring(name.lastIndexOf('$') + 1);
//          }
//
//          name += ": " + ((AbstractDocument.AbstractElement) value).getName();
//          AttributeSet set = ((AbstractDocument.AbstractElement) value).getAttributes();
//          Enumeration names = set.getAttributeNames();
//          int i = 0;
//          while (names.hasMoreElements()) {
//            Object attrName = names.nextElement();
//            if (!"name".equals(attrName.toString())) {
//              if (i > 0) {
//                name += ", ";
//              } else {
//                name += "(";
//              }
//
//              name += attrName + "(" + ClassUtil.getShortClassName(attrName) + ") = ";
//              name += set.getAttribute(attrName) + "("
//                  + ClassUtil.getShortClassName(set.getAttribute(attrName)) + ")";
//              ++i;
//            }
//          }
//          if (i > 0) {
//            name += ")";
//          }
//          return name;
//        } else {
//          return value.toString();
//        }
//      }
//    };
//
//    RitDialog dialog = RitDialog.create(
//        IDEController.getInstance.createProjectContext());
//
//    JScrollPane pane = new JScrollPane(tree);
//    dialog.getContentPane().add(pane);
//    dialog.setSize(1000, 1000);
//    SwingUtil.centerDialogOnScreen(dialog);
//    tree.expandAll();
//
//    dialog.show();
//  }
}
