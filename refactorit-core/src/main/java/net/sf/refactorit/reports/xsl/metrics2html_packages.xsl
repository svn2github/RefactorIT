<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent = "no"/>

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
	<xsl:apply-templates select="info"/>
	
	<table class="details" cellspacing="0">
  		<tr class="detailsHeader">
		  <td>Location</td>
		  <td>Type</td>
		  
	        <xsl:for-each select="info/metric">
	        	<td>
	        	<acronym>
	        		<xsl:attribute name="title"><xsl:value-of select="@description"/></xsl:attribute>
	        		<xsl:value-of select="@name"/>
					</acronym>
				</td>
			</xsl:for-each>
		</tr>

		<xsl:for-each select="descendant::*[count(child::metric) > 0][name() != 'info'][name() != 'class'][name() != 'interface'][name() != 'method'][name() != 'field'][name() != 'constructor']">
			<xsl:call-template name="printMetricsDetails">
				<xsl:with-param name="evenRecord" select="boolean(position() mod 2 = 0)"/>
			</xsl:call-template>
		</xsl:for-each>		
	</table>
</xsl:template>

<xsl:template match="info">
<table>
    <tr>
      <td class="metricsInfo">
        Metrics information
      </td>
    </tr>
    <tr>
      <td>
        <table class="info">
	        <tr>
		        <td>
		        	<b>Name</b>
		        </td>
		        <td width="100%">
		        	<b>Range</b>
		        </td>
	        </tr>
	        
	        <xsl:for-each select="metric">
	        <tr>
            <xsl:if test="position() mod 2 = 0">
 	           <xsl:attribute name="class">gray</xsl:attribute>
            </xsl:if>
	        	<td><xsl:value-of select="@name"/> (<xsl:value-of select="@description"/>)</td>
	        	<td>
	        		<xsl:value-of select="@lower-preffered-limit"/>
	        		<xsl:text> ... </xsl:text>
	        		<xsl:value-of select="@upper-preffered-limit"/>
	        		</td>
	        </tr>
			</xsl:for-each>
		
		</table>
      </td>
    </tr>
</table>
</xsl:template>

<xsl:template name="printMetricsDetails">
	<xsl:param name="evenRecord">
		<xsl:value-of select="false()"/>
	</xsl:param>

	<xsl:variable name="current" select="."/>

		<tr>
		    <xsl:if test="$evenRecord">
 	           <xsl:attribute name="class">gray</xsl:attribute>
            </xsl:if>
			<td><xsl:call-template name="printLocation"/></td>
			<td><xsl:value-of select="name()"/></td>
			<xsl:for-each select="/report/info/metric">
				<xsl:choose> 
					<xsl:when test="$current/metric[@name = current()/@name]"> 
						<xsl:variable name="value" select="$current/metric[@name = current()/@name]/@value"/>
						<td><xsl:value-of select="format-number($value,'#.###')"/></td>
					</xsl:when> 
					<xsl:otherwise> 
						<td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
					</xsl:otherwise> 
				</xsl:choose> 
			</xsl:for-each>
		</tr>
</xsl:template>

<xsl:template match="metric">
</xsl:template>

     <xsl:template name="printLocation">
        <xsl:for-each select="ancestor-or-self::*[ancestor::report][name() != 'children'][name() != 'metric']">
        			<xsl:value-of select="@name"/>
                    <xsl:if test="position() != last()">
                            <xsl:text>.</xsl:text>
                    </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
<!-- STATIC TEXT PRINTING TEMPLATES -->
<xsl:template name="printHead">
<head>
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

.metricsInfo {
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

.info td {
 white-space: nowrap;
 padding-left: 5px;
 font-size: small;
}

.gray {
  background-color: #D7D7D7;
}

acronym {
  cursor:help;
  text-decoration: underline; 
}
</style>
</head>
</xsl:template>

</xsl:stylesheet>