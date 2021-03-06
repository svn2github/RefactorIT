<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="1.0">
  <!-- title -->
  <title>RefactorIT User Manual</title>

  <!-- maps -->
  <maps>
     <homeID>.top</homeID>
     <mapref location="helpMap.jhm"/>
  </maps>
  

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Table Of Contents</label>
    <type>javax.help.TOCView</type>
    <data>helpTOC.xml</data>
  </view>
  
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>

  <!-- We do not need them now
  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>helpIndex.xml</data>
  </view>
  -->
</helpset>
