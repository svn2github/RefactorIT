/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.pmd.InterfaceToPMD;
import net.sf.refactorit.audit.rules.service.ServiceAnnotationUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceBinItemReferenceRule;
import net.sf.refactorit.audit.rules.service.ServiceEnumUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceForinUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceGenericsUsagesRule;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.audit.AuditAction;
import net.sf.refactorit.ui.module.dependencies.DependencyLoopsAction;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;

import org.apache.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;


public class Statistics {
  public static class StatisticsData {
    DateFormat df = DateFormat.getInstance();

    private String name;

    private String group;

    private Date firstUse;

    private Date lastUse;

    private int total;

    private String key;

    private boolean needsUpdate = true;

    private float intervalBetweenUsages;

    public StatisticsData(String name, String group, Date firstUse,
        Date lastUse, int total, String key) {
      this.name = name;
      this.group = group;
      this.firstUse = firstUse;
      this.lastUse = lastUse;
      this.total = total;
      this.key = key;
    }

    public StatisticsData(String name, String group, String firstUse,
        String lastUse, String total, String key)/* throws ParseException */{

      this.name = name;
      this.group = group;
      try {
        this.firstUse = df.parse(firstUse);
        this.lastUse = df.parse(lastUse);
      } catch (ParseException e) {
        // throw new RuntimeException(e.getMessage());
      }
      this.total = Integer.parseInt(total);
      this.key = key;
    }

    public StatisticsData(String name, String group, Date firstUse, String key) {
      this(name, group, firstUse, firstUse, 1, key);
    }

    public String getName() {
      return name;
    }

    public String getGroup() {
      return group;
    }

    public String getKey() {
      return key;
    }

    public Date getFirstUse() {
      return firstUse;
    }

    public Date getLastUse() {
      return lastUse;
    }

    public int getTotal() {
      return total;
    }

    public void setLastUse(String date)/* throws Exception */{
      try {
        lastUse = df.parse(date);
      } catch (ParseException e) {
        // throw new RuntimeException(e.getMessage());
      }
    }

    public void setFirstUse(String date)/* throws Exception */{
      try {
        firstUse = df.parse(date);
      } catch (ParseException e) {
        // throw new RuntimeException(e.getMessage());
      }
    }

    public void setTotal(int total) {
      this.total = total;
    }

    public void addUsage() {
      if (firstUse == null)
        firstUse = Calendar.getInstance().getTime();
      lastUse = Calendar.getInstance().getTime();
      total++;// currentUsages.add(Calendar.getInstance().getTime());
      recalculateIntervalBetweenUsages();
    }

    private void recalculateIntervalBetweenUsages() {
      if (firstUse == null || lastUse == null)
        intervalBetweenUsages = Float.MAX_VALUE;
      else
        intervalBetweenUsages = (lastUse.getTime() - firstUse.getTime())
            / (1000 * total);
    }

    public String toString() {
      return name + ": " + firstUse + " to " + lastUse + " total " + total;
    }

    // public Float getIntervalBetweenUsages(){
    // return new Float(intervalBetweenUsages);
    // }
    //    
    // public String getIntervalBetweenUsagesAsString(){
    // if(intervalBetweenUsages < 60)
    // return "Once a minute";
    // else if(intervalBetweenUsages < 3600)
    // return "Once an hour";
    // else if(intervalBetweenUsages < 86400)
    // return "Once a day";
    // else if(intervalBetweenUsages < 604800)
    // return "Once a week";
    // else if(intervalBetweenUsages < 18144000)
    // return "Once a month";
    // else
    // return "Rare";
    // }
  }

  public static String statsPath = GlobalOptions.configDir + File.separator;

  private static String ROOT_ELEMENT_NAME = "statistics";

  private static String CATEGORY_ELEMENT_NAME = "category";

  private static String ENTRY_ELEMENT_NAME = "entry";

  private static String NAME_ELEMENT_NAME = "name";

  private static String FIRST_USE_ELEMENT_NAME = "first_use";

  private static String LAST_USE_ELEMENT_NAME = "last_use";

  private static String TOTAL_ELEMENT_NAME = "total";

  private static String KEY_ATTRIBUTE_NAME = "key";

  private static String TYPE_ATTRIBUTE_NAME = "type";

  public static String CATEGORY_AUDITS = "audits";

  public static String CATEGORY_METRICS = "metrics";

  public static String CATEGORY_COMMON = "common";

  private HashMap currentStats = new HashMap();

  private static Logger logger = AppRegistry.getLogger(Statistics.class);

  private static Statistics instance;

  public StatisticsData[] getStatisticsData(String category) {
    ArrayList l = new ArrayList();
    HashMap m = (HashMap) currentStats.get(category);

    if (m == null)
      return new StatisticsData[] {};

    for (Iterator j = m.keySet().iterator(); j.hasNext();)
      l.add(m.get(j.next()));

    return (StatisticsData[]) l.toArray(new StatisticsData[] {});
  }

  //
  // public static void init() {
  // instance = new Statistics();
  // }

  public static Statistics getInstance() {
    if (instance == null)
      instance = new Statistics();
    return instance;
  }

  // Adds the usage of some action to the current statistics and to the log
  public void addUsage(String category, String key, String name, String group) {// throws
    // Exception
    // {
    DateFormat df = DateFormat.getInstance();
    Date date = Calendar.getInstance().getTime();

    if (isValidAction(key)) {

      if (!currentStats.containsKey(category)) {
        HashMap m = new HashMap();
        m.put(key, new StatisticsData(name, group, date, key));
        currentStats.put(category, m);
      } else {
        HashMap m = (HashMap) currentStats.get(category);
        if (m.containsKey(key))
          ((StatisticsData) m.get(key)).addUsage();
        else
          m.put(key, new StatisticsData(name, group, date, key));
      }
      try {
        PrintWriter out = new PrintWriter(new FileWriter(statsPath
            + "stats.log", true));
        out.println(category + "," + key + "," + name + "," + df.format(date)
            + "," + group);
        out.close();
      } catch (IOException e) {
        logger.error("Failed to log statistics: writing denied", e);
      }
    }
  }

  private Statistics() {
    File fXML = new File(statsPath + "stats.xml");
    Document doc = null;
    DateFormat df = DateFormat.getInstance();

    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      doc = builder.parse(fXML);
    } catch (Exception e) {
    }

    currentStats.put(CATEGORY_COMMON, getAllCommonActions());
    currentStats.put(CATEGORY_AUDITS, getAllAudits());
    currentStats.put(CATEGORY_METRICS, getAllMetrics());

    if (doc != null) {
      NodeList categories = doc.getElementsByTagName(CATEGORY_ELEMENT_NAME);
      for (int i = 0; i < categories.getLength(); i++) {
        Element category = (Element) categories.item(i);
        String type = category.getAttribute(TYPE_ATTRIBUTE_NAME);
        Map m = (Map) currentStats.get(type);
        NodeList entries = category.getChildNodes();
        for (int j = 0; j < entries.getLength(); j++) {
          Element entry = (Element) entries.item(j);
          String key = entry.getAttribute(KEY_ATTRIBUTE_NAME);
          String name = getTextContent(entry.getElementsByTagName(
              NAME_ELEMENT_NAME).item(0));
          String firstUse = getTextContent(entry.getElementsByTagName(
              FIRST_USE_ELEMENT_NAME).item(0));
          String lastUse = getTextContent(entry.getElementsByTagName(
              LAST_USE_ELEMENT_NAME).item(0));
          String total = getTextContent(entry.getElementsByTagName(
              TOTAL_ELEMENT_NAME).item(0));

          if (m.containsKey(key)) {
            StatisticsData d = (StatisticsData) m.get(key);
            d.setFirstUse(firstUse);
            d.setLastUse(lastUse);
            d.setTotal(Integer.parseInt(total));
          } else
            m.put(key, new StatisticsData(name, null, firstUse, lastUse, total,
                key));
        }
        currentStats.put(type, m);
      }
    }
  }

  private static String getTextContent(Node n) {
    if (n.getFirstChild() instanceof CharacterData)
      return ((CharacterData) n.getFirstChild()).getData();
    else
      return null;
  }

  // Updates the statistics according to the log file
  public static void updateStats() {
    File fLog = new File(statsPath + "stats.log");
    File fXML = new File(statsPath + "stats.xml");
    BufferedReader in = null;
    Document doc = null;
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      builder.setErrorHandler(null);
      if (!fXML.exists()) {
        fXML.createNewFile();
        doc = builder.newDocument();
        doc.appendChild(doc.createElement(ROOT_ELEMENT_NAME));
      } else
        doc = builder.parse(fXML);
      if (fLog.exists()) {
        DateFormat df = DateFormat.getInstance();
        HashMap dataHolder = new HashMap();
        String line;
        in = new BufferedReader(new FileReader(fLog));
        while ((line = in.readLine()) != null) {
          String[] data = line.split(",");
          HashMap m = (HashMap) dataHolder.get(data[0]); // data[0] is a
          // category
          // name
          if (m == null) {
            m = new HashMap();
            dataHolder.put(data[0], m);
          }
          if (!m.containsKey(data[1])) // data[1] is a key
            m.put(data[1], new StatisticsData(data[2], null, data[3], data[3],
                "1", data[1]));
          else {
            ((StatisticsData) m.get(data[1])).addUsage();
          }
        }

        NodeList categories = doc.getElementsByTagName(CATEGORY_ELEMENT_NAME);
        for (int i = 0; i < categories.getLength(); i++) {
          Element category = (Element) categories.item(i);
          String type = category.getAttribute(TYPE_ATTRIBUTE_NAME);
          if (dataHolder.containsKey(type)) {
            HashMap m = (HashMap) dataHolder.get(type);
            // System.out.println(m);
            NodeList entries = category.getChildNodes();
            for (int j = 0; j < entries.getLength(); j++) {
              Element entry = (Element) entries.item(j);
              String key = entry.getAttribute(KEY_ATTRIBUTE_NAME);
              if (m.containsKey(key)) {
                Statistics.StatisticsData data = (Statistics.StatisticsData) m
                    .get(key);
                Node oldLastUsedDateNode = entry.getElementsByTagName(
                    LAST_USE_ELEMENT_NAME).item(0).getFirstChild();
                oldLastUsedDateNode.setNodeValue(df.format(data.getLastUse()));
                Node oldTotalNode = entry.getElementsByTagName(
                    TOTAL_ELEMENT_NAME).item(0);
                int oldTotal = Integer
                    .parseInt(/* getData((Node) oldTotalNode) */getTextContent(oldTotalNode));
                oldTotalNode.getFirstChild().setNodeValue(
                    (new Integer(oldTotal + data.getTotal())).toString());
                m.remove(key);
              }
            }
            if (m.isEmpty())
              dataHolder.remove(type);
          }
        }

        if (!dataHolder.isEmpty()) {
          addDocumentEntry(doc, dataHolder);
          for (Iterator i = dataHolder.keySet().iterator(); i.hasNext();) {
            String type = (String) i.next();
            HashMap m = (HashMap) dataHolder.get(type);
            for (Iterator j = m.keySet().iterator(); j.hasNext();) {
              String key = (String) j.next();
            }
          }
        }

        in.close();
        fLog.delete();
      }
      DOMSource src = new DOMSource(doc);
      Transformer transformer = TransformerFactory.newInstance()
          .newTransformer();
      StreamResult sr = new StreamResult(new FileWriter(fXML));
      transformer.transform(src, sr);

    } catch (SAXParseException e) {
      // delete if unable to parse XML
      fXML.delete();
    } catch (Exception e) {
      logger.error("Failed to update the statistics", e);
    }
    // return doc;
  }

  // Adds a new node of type entry to the document that holds the statistics
  private static void addDocumentEntry(Document doc, HashMap dataHolder) {
    DateFormat df = DateFormat.getInstance();
    Set keys = dataHolder.keySet();

    NodeList categories = doc.getElementsByTagName(CATEGORY_ELEMENT_NAME);
    for (int i = 0; i < categories.getLength(); i++) {
      Element category = (Element) categories.item(i);
      String type = category.getAttribute(TYPE_ATTRIBUTE_NAME);
      if (keys.contains(type)) {
        HashMap m = (HashMap) dataHolder.get(type);
        for (Iterator j = m.keySet().iterator(); j.hasNext();) {
          String key = (String) j.next();
          StatisticsData d = (StatisticsData) m.get(key);
          Element entryElement = doc.createElement(ENTRY_ELEMENT_NAME);
          entryElement.setAttribute(KEY_ATTRIBUTE_NAME, key);
          Element nameElement = doc.createElement(NAME_ELEMENT_NAME);
          Text nameValue = doc.createTextNode(d.getName());
          Element firstUseElement = doc.createElement(FIRST_USE_ELEMENT_NAME);
          Text dateValue1 = doc.createTextNode(df.format(d.getLastUse()));
          Element lastUseElement = doc.createElement(LAST_USE_ELEMENT_NAME);
          Text dateValue2 = doc.createTextNode(df.format(d.getLastUse()));
          Element totalElement = doc.createElement(TOTAL_ELEMENT_NAME);
          Text totalValue = doc.createTextNode((new Integer(d.getTotal())
              .toString()));
          totalElement.appendChild(totalValue);
          firstUseElement.appendChild(dateValue1);
          lastUseElement.appendChild(dateValue2);
          nameElement.appendChild(nameValue);
          entryElement.appendChild(nameElement);
          entryElement.appendChild(firstUseElement);
          entryElement.appendChild(lastUseElement);
          entryElement.appendChild(totalElement);
          category.appendChild(entryElement);
          Element statistics = doc.getDocumentElement();
          statistics.appendChild(category);
          // System.out.println("Added to stats.xml: " + d.getName() + " "
          // + d.getLastUse() + " " + d.getTotal());
        }
        dataHolder.remove(type);
      }
    }

    if (!dataHolder.isEmpty()) {
      for (Iterator i = dataHolder.keySet().iterator(); i.hasNext();) {
        String type = (String) i.next();
        HashMap m = (HashMap) dataHolder.get(type);
        Element category = doc.createElement(CATEGORY_ELEMENT_NAME);
        category.setAttribute(TYPE_ATTRIBUTE_NAME, type);
        for (Iterator j = m.keySet().iterator(); j.hasNext();) {
          String key = (String) j.next();
          StatisticsData d = (StatisticsData) m.get(key);
          Element entryElement = doc.createElement(ENTRY_ELEMENT_NAME);
          entryElement.setAttribute(KEY_ATTRIBUTE_NAME, key);
          Element nameElement = doc.createElement(NAME_ELEMENT_NAME);
          Text nameValue = doc.createTextNode(d.getName());
          Element firstUseElement = doc.createElement(FIRST_USE_ELEMENT_NAME);
          Text dateValue1 = doc.createTextNode(df.format(d.getLastUse()));
          Element lastUseElement = doc.createElement(LAST_USE_ELEMENT_NAME);
          Text dateValue2 = doc.createTextNode(df.format(d.getLastUse()));
          Element totalElement = doc.createElement(TOTAL_ELEMENT_NAME);
          Text totalValue = doc.createTextNode((new Integer(d.getTotal())
              .toString()));
          totalElement.appendChild(totalValue);
          firstUseElement.appendChild(dateValue1);
          lastUseElement.appendChild(dateValue2);
          nameElement.appendChild(nameValue);
          entryElement.appendChild(nameElement);
          entryElement.appendChild(firstUseElement);
          entryElement.appendChild(lastUseElement);
          entryElement.appendChild(totalElement);
          // Element statistics = doc.getDocumentElement();
          category.appendChild(entryElement);
          doc.getDocumentElement().appendChild(category);
        }
      }
    }

  }

  private static Map getAllAudits() {
    Map result = new java.util.HashMap();
    Audit[] rules = Audit.getAllAudits();
    for (int i = 0; i < rules.length; i++) {
      AuditRule a = rules[i].createAuditingRule();
      if (isValidAction(a.getKey()))
        result.put(a.getKey(), new Statistics.StatisticsData(a.getAuditName(),
            a.getCategoryName(), null, null, 0, a.getKey()));
    }

    // PMD
    for (Iterator a = InterfaceToPMD.getAvailableRuleSets().iterator(); a
        .hasNext();) {
      RuleSet set = (RuleSet) a.next();
      String category = set.getName();
      for (Iterator b = set.getRules().iterator(); b.hasNext();) {
        Rule rule = (Rule) b.next();
        String key = (new Audit(rule)).getKey();
        String name = StringUtil.splitCamelStyleIntoWords(rule.getName());
        result.put(key, new StatisticsData(name, category, null, null, 0, key));
      }
    }
    return result;
  }

  private static boolean isValidAction(String key) {
    return !key.equals(AuditAction.KEY) && !key.equals(MetricsAction.KEY)
        && !key.equals(DependencyLoopsAction.KEY)
        && !key.equals(ServiceGenericsUsagesRule.NAME)
        && !key.equals(ServiceEnumUsagesRule.NAME)
        && !key.equals(ServiceAnnotationUsagesRule.NAME)
        && !key.equals(ServiceForinUsagesRule.NAME)
        && !key.equals(ServiceBinItemReferenceRule.NAME)
        && !key.equals("other");
  }

  private static HashMap getAllCommonActions() {
    HashMap l = new HashMap();
    List actions = ModuleManager.getAllActions();
    for (int i = 0; i < actions.size(); i++) {
      RefactorItAction action = (RefactorItAction) actions.get(i);
      if (isValidAction(action.getKey()))
        l.put(action.getKey(), new StatisticsData(action.getName(), null, null,
            null, 0, action.getKey()));
    }

    return l;
  }

  private static Map getAllMetrics() {
    ResourceBundle resLocalizedStrings = ResourceUtil
        .getBundle(MetricsAction.class);
    Map result = new HashMap();
    for (int i = 1; i < MetricsAction.METRICS; i++) {
      String key = MetricsAction.getDefaultKey(i);// GlobalOptions.getOption("metric.column."
      // + i);
      if (isValidAction(key)) {
        String name = resLocalizedStrings.getString(key + ".tooltip");
        result.put(key, new Statistics.StatisticsData(name, null, null, null,
            0, key));
      }
    }
    return result;
  }
}

/*
 * Map allActions = getAllCommonActions(); HashMap m = (HashMap)
 * currentStats.get(CATEGORY_COMMON); if(m == null){ m = new HashMap();
 * currentStats.put(CATEGORY_COMMON, m); } for(Iterator
 * i=allActions.keySet().iterator(); i.hasNext();){ String key = (String)
 * i.next(); String name = (String) allActions.get(key); if(!m.containsKey(key))
 * m.put(key, new StatisticsData(name, null, null, 0)); }
 */
/*
 * if(type.equals(CATEGORY_COMMON)){ m = getAllCommonActions();
 * System.out.println("Common: " + m); }else if(type.equals(CATEGORY_AUDITS)){ m =
 * getAllAudits(); System.out.println("Audits: " + m); }else{ m =
 * getAllMetrics(); System.out.println("Metrics: " + m); }
 */
