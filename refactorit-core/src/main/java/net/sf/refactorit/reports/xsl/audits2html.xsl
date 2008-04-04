<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent = "no"/>

<xsl:variable name = "violations" select="/report//violation"/>
<xsl:variable name = "audits" select="/report/info/audit[@id = $violations/@audit]"/>
<xsl:variable name = "categories" select="/report/info/audit[not(@category = preceding-sibling::audit/@category)]"/>
<xsl:variable name="notEmptyCategories" select="$categories[@category = $audits/@category]"/>

<xsl:template match="/">
<html>
  <xsl:call-template name="printHead"/>
  <body>
  <xsl:apply-templates select="report"/>
  </body>
</html>
</xsl:template>

<xsl:template name="printPriorityTotal">
	<xsl:param name="priority_name" select="'NORMAL'"/>
	<xsl:value-of select="$priority_name"/>
	<xsl:text>: </xsl:text>
	<xsl:value-of select="count($violations[@audit = $audits[@priority = $priority_name]/@id])"/>
</xsl:template>


<xsl:key name="audit-by-category" match="@category" use="." />

<xsl:template match="report">
<b>RefactorIT Audits</b><br/>
<table>
    <tr>
      <td>
        <h3 align="center">Report created at <xsl:value-of select = "@date"/></h3>
      </td>
    </tr>
    <tr align="center">
      <td>Violation Summary - 
      <xsl:call-template name="printPriorityTotal">
      	<xsl:with-param name="priority_name" select="'HIGH'"/>
      </xsl:call-template>
      <xsl:text>&#10;</xsl:text>
      
      <xsl:call-template name="printPriorityTotal">
      	<xsl:with-param name="priority_name" select="'NORMAL'"/>
      </xsl:call-template>
      <xsl:text>&#10;</xsl:text>
      
      <xsl:call-template name="printPriorityTotal">
      	<xsl:with-param name="priority_name" select="'LOW'"/>
      </xsl:call-template>
      <xsl:text>&#10;</xsl:text>
	  </td>
    </tr>
    <tr>
      <td>
      <hr/></td>
    </tr>
    <tr>
      <td><b>Summary:</b></td>
    </tr>
</table>
 <table>
    <xsl:for-each select="$notEmptyCategories">
		<tr><td class="category">
			<a>
				<xsl:attribute name="id"><xsl:value-of select="@category"/></xsl:attribute>
			</a>
			<xsl:value-of select="@category"/>
			</td></tr>
		<xsl:for-each select="$audits[@category = current()/@category]">
			<tr><td>
			  <a class="indent">
				<xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute>
				<xsl:value-of select="@name"/>
				[<xsl:value-of select="count($violations[@audit = current()/@id])"/>] 
			  </a>
			</td></tr>
		</xsl:for-each>
	</xsl:for-each>
    </table>
    <table>
          <tr>
            <td><hr/></td>
          </tr>
          <tr>
            <td><b>Details:</b></td>
          </tr>
          <tr>
            <td>
              <table class="details" cellspacing="0">
            	<xsl:for-each select="$audits">
	                <tr>
	                	<xsl:attribute name="class">
							<xsl:choose> 
								<xsl:when test="@priority = 'LOW'"> 
									<xsl:value-of select="'lowPriority'"/> 
								</xsl:when> 
								<xsl:when test="@priority = 'HIGH'"> 
									<xsl:value-of select="'highPriority'"/>
								</xsl:when> 
								<xsl:otherwise> 
									<xsl:value-of select="'normalPriority'"/> 
								</xsl:otherwise> 
							</xsl:choose> 
	                	</xsl:attribute>
	                  <td class="leftTd">
	                  	<a>
		                  	<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
						</a>
						<xsl:value-of select="@name"/>
						<xsl:text>&#10;</xsl:text>
						<a>
		                  	<xsl:attribute name="href">#<xsl:value-of select="@category"/></xsl:attribute>
		                  	<xsl:attribute name="style">font-size: x-small; color: #000000;</xsl:attribute>
		                  	<xsl:text>[top]</xsl:text>
		                </a>
	                  	</td>
	                  <td>Corrective Action</td>
	                  <td>Location</td>
	                  <td>Line</td>
   	                  <td>Density</td>
	                </tr>
                
                
					<xsl:for-each select="$violations[@audit = current()/@id]">
		                <tr>
		                <xsl:if test="position() mod 2 = 0">
		                <xsl:attribute name="class">gray</xsl:attribute>
		                </xsl:if>
		                  <td class="leftTd"><xsl:value-of select="@message"/></td>
		                  <td>
		                  	<xsl:if test="count(corrective-action) = 0">
			                  	<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
		                  	</xsl:if>
		                  	<xsl:for-each select="corrective-action">
		                  		<xsl:if test="count(parent::node()/corrective-action) > 1">
			                  		<xsl:value-of select="position()"/><xsl:text>. </xsl:text>
		                  		</xsl:if>
		                  		<xsl:value-of select="@name"/>
		                  		<xsl:if test="position() != last()">
		                  			<br/>
		                  		</xsl:if>
		                  	</xsl:for-each></td>
		                  <td><xsl:call-template name="printLocation"/></td>
		                  <td><xsl:value-of select="@line"/></td>
		                  <td><xsl:value-of select="@density"/></td>
		                </tr>
					</xsl:for-each>
				</xsl:for-each>
              </table>
            </td>
          </tr>
     </table>
</xsl:template>

         <xsl:template name="printLocation">
            <xsl:for-each select="ancestor::*[ancestor::report][name() != 'children']">
            			<xsl:value-of select="@name"/>
                        <xsl:if test="position() != last()">
                                <xsl:text>.</xsl:text>
                        </xsl:if>
            </xsl:for-each>
        </xsl:template>


<!-- STATIC TEXT PRINTING TEMPLATES -->
<xsl:template name="printHead">
<head>
  <title>RefactorIT Audits</title>
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

table td {
  vertical-align: top;
}

hr {
  width: 100%; 
  height: 2px;
}  
  
A {
  font-size: small;
  font-family: Tahoma, Verdana, Helvetica;
  font-weight: bold; 
  color: #FB7216;
  text-decoration: none;
}

A:hover {
  text-decoration: underline; 
}

.category {
  width: 50%; 
  background-color: #274268; 
  color: #FFFFFF;
  vertical-align: top; 
  text-align: center;
  font-weight: bold;
  padding: 2px;
}

.indent {
  margin-left: 40px;
}

.bold {
  font-weight: bold;
}

.details {
  font-size: small;
  border-left: solid 1px black;
  border-top: solid 1px black;
  border-bottom: solid 1px black;
}


.details td {
 white-space: nowrap;
 border-right: solid 1px black;
 padding-left: 5px;
}

.details .leftTd {

}

.highPriority {
  color: #FFFFFF; 
  background-color: #8C0000;
}

.lowPriority {
  color: #000000; 
  background-color: #FFFF48;
}

.gray {
  background-color: #D7D7D7;
}

.normalPriority {
  color: #FFFFFF; 
  background-color: #3366FF;
}
</style>
</head>
</xsl:template>


</xsl:stylesheet>
