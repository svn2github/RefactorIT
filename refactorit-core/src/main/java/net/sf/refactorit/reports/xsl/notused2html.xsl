<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


<xsl:variable name="totalNotUsedWholeTypes" select="count(/report/whole-types//*[not(*)])"/> 

<xsl:variable name="totalNotUsedSingleMembers" select="count(/report/single-members//*[not(*)])"/> 


<xsl:template match="/">
<html>
	<xsl:call-template name="printHead"/>
	<body>
	<xsl:apply-templates select="report"/>
	</body>
</html>
</xsl:template>


<xsl:template match="report">
	<table>
	    <tr>
	      <td>
	        <h3 align="center">Report created at <xsl:value-of select="@date"/></h3>
	      </td>
	    </tr>
	</table>


	<table>
		<tr class="info">
			<td>Not used statistics</td>
		</tr>
		<tr><td>whole classes: <xsl:value-of select="$totalNotUsedWholeTypes"/></td></tr>
		<tr class="gray"><td>single members: <xsl:value-of select="$totalNotUsedSingleMembers"/></td></tr>
		<tr><td>total: <xsl:value-of select="$totalNotUsedWholeTypes+$totalNotUsedSingleMembers"/></td></tr>
	</table>
		
	<xsl:apply-templates select="info/exclude-filter-rules"/> 
	<xsl:apply-templates select="whole-types"/>
	<xsl:apply-templates select="single-members"/>
</xsl:template>


<xsl:template match="exclude-filter-rules"> 
<table>
	<tr class="info">
		<td colspan="3">Used exclude filter rules</td>
	</tr>
	<tr>
		<td><b>Rule ID</b></td>
		<td><b>Rule name</b></td>
		<td><b>Rule options</b></td>
	</tr>
	<xsl:for-each select="rule">
       <xsl:call-template name="rulePrinting">
       	 <xsl:with-param name="evenRule" select="boolean(position() mod 2 = 0)"/>
       </xsl:call-template>
	</xsl:for-each> 
</table>
</xsl:template>

	
<xsl:template name="rulePrinting"> 
	<xsl:param name="evenRule" select="false()"/>
	
	<tr>
	    <xsl:if test="$evenRule">
 	       <xsl:attribute name="class">gray</xsl:attribute>
        </xsl:if>
	<td><xsl:value-of select="@id"/></td>
	<td><xsl:value-of select="@name"/></td>
	<td>
	<xsl:for-each select="option">
	   <xsl:call-template name="optionPrinting"/>
	</xsl:for-each> 
	<xsl:text>&#160;</xsl:text>
	</td>
	</tr>
	</xsl:template>

		<xsl:template name="optionPrinting">
			<xsl:if test="position()&gt;1">
				<br/>
	 		</xsl:if> 
			option "<xsl:value-of select="@name"/>" is "<xsl:value-of select="@value"/>"
		</xsl:template>
		
		
<xsl:template match="whole-types"> 
<table class="details" cellspacing="0">
<tr>
<td colspan="3" class="detailsHeader">Not used whole classes [<xsl:value-of select="$totalNotUsedWholeTypes"/>]:</td>
</tr>
<tr>
<td><b>Package</b></td>
<td><b>Class</b></td>
<td><b>Type</b></td>
</tr>
<xsl:for-each select="/report/whole-types//descendant::*[not(child::*)]">
	 <xsl:sort select = "ancestor::*[self::package]/attribute::name" /> 
	 <xsl:sort select = "ancestor-or-self::*[ancestor::package]/attribute::name"/>
	 <xsl:sort select = "name()" />
      <xsl:call-template name="wholeTypesPrinting">
      	<xsl:with-param name="evenRecord" select="boolean(position() mod 2 = 0)"/>
      </xsl:call-template>
</xsl:for-each>
</table>
</xsl:template>
	
	<xsl:template name="wholeTypesPrinting">
	<xsl:param name="evenRecord" select="false()"/>
	<tr>
	    <xsl:if test="$evenRecord">
 		    <xsl:attribute name="class">gray</xsl:attribute>
        </xsl:if>
	<td><xsl:value-of select="ancestor::*[self::package]/attribute::name"/></td>
	<td><xsl:call-template name="printWholeTypeName"/></td>
	<td><xsl:value-of select="name()"/></td>
	</tr>
	</xsl:template>
        
             <xsl:template name="printWholeTypeName">
                <xsl:for-each select="ancestor-or-self::*[ancestor::package]">
                            <xsl:value-of select="@name"/>
                            <xsl:if test="position() != last()">
                                    <xsl:text>.</xsl:text>
                            </xsl:if>
                </xsl:for-each>
            </xsl:template>
	
<xsl:template match="single-members">
<table class="details" cellspacing="0">
<tr>
<td colspan="4" class="detailsHeader">Not used single members [<xsl:value-of select="$totalNotUsedSingleMembers"/>]:</td>
</tr>
<tr>
<td><b>Package</b></td>
<td><b>Class</b></td>
<td><b>Member</b></td>
<td><b>Member type</b></td>
</tr>

<xsl:for-each select="/report/single-members//descendant::*[not(child::*)]">
	 <xsl:sort select = "ancestor::*[self::package]/attribute::name" /> 
	 <xsl:sort select = "ancestor::*[ancestor::package]/attribute::name"/>
	 <xsl:sort select = "name()" />
      <xsl:call-template name="singleMembersPrinting">
	      <xsl:with-param name="evenRecord" select="boolean(position() mod 2 = 0)"/>
      </xsl:call-template>
</xsl:for-each>
</table>
</xsl:template>


	<xsl:template name="singleMembersPrinting">
	<xsl:param name="evenRecord" select="false()"/>
	<tr>
	    <xsl:if test="$evenRecord">
 		    <xsl:attribute name="class">gray</xsl:attribute>
        </xsl:if>
	<td><xsl:value-of select="ancestor::*[self::package]/attribute::name"/></td>
	<td><xsl:call-template name="printFullClassName"/></td>
	<td><xsl:value-of select="@name"/></td>
	<td><xsl:value-of select="name()"/></td>
	</tr>
	</xsl:template>
        
            <xsl:template name="printFullClassName">
                <xsl:for-each select="ancestor::*[ancestor::package]">
                            <xsl:value-of select="@name"/>
                            <xsl:if test="position() != last()">
                                    <xsl:text>.</xsl:text>
                            </xsl:if>
                </xsl:for-each>
            </xsl:template>
            
 
<!-- STATIC TEXT PRINTING TEMPLATES -->
<xsl:template name="printHead">
<head>
<title>Not used report</title>
  <style type="text/css">
body {
  font-family: Tahoma, Verdana, Helvetica;
}

table {
  text-align: left; 
  width: 100%;
  padding: 0px;
  border-width: 0px;
}

.info {
  width: 100%; 
  background-color: #274268; 
  color: #FFFFFF;
  vertical-align: top; 
  text-align: left;
  font-weight: bold;
  padding-left: 10px;
} 

.detailsHeader {
  width: 100%; 
  background-color: #FB7216; 
  color: #FFFFFF;
  vertical-align: top; 
  text-align: center;
  font-weight: bold;
  padding: 2px;
}

.details {
  font-size: small;
  border-left: solid 1px black;
  border-top: solid 1px black;
  border-bottom: solid 1px black;
}

.details td {
 white-space: nowrap;
 padding-left: 5px;
 border-right: solid 1px black;
}


.gray {
  background-color: #D7D7D7;
}
</style>
</head>
</xsl:template>

</xsl:stylesheet>
