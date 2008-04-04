<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent = "no"/>

<xsl:template match="/">
  <xsl:apply-templates select="report"/>
</xsl:template>

<xsl:template match="report">
	<xsl:text>Location</xsl:text><xsl:text> &#9; </xsl:text>
	<xsl:text>Type</xsl:text><xsl:text> &#9; </xsl:text>
	
	<xsl:for-each select="info/metric">
   		<xsl:value-of select="@name"/><xsl:text> &#9; </xsl:text>
	</xsl:for-each>
	<xsl:text>&#10;</xsl:text>

	<xsl:for-each select="descendant::*[count(child::metric) > 0][name() != 'info']">
			<xsl:variable name="current" select="."/>
			
			<xsl:call-template name="printLocation"/><xsl:text> &#9; </xsl:text>
			<xsl:value-of select="name()"/><xsl:text> &#9; </xsl:text>
			<xsl:for-each select="/report/info/metric">
				<xsl:choose> 
					<xsl:when test="$current/metric[@name = current()/@name]"> 
						<xsl:variable name="value" select="$current/metric[@name = current()/@name]/@value"/>
						<xsl:value-of select="format-number($value,'#.###')"/><xsl:text> &#9; </xsl:text>
					</xsl:when> 
					<xsl:otherwise> 
						<xsl:text> &#9; </xsl:text>
					</xsl:otherwise> 
				</xsl:choose> 
			</xsl:for-each>
			
			<xsl:text>&#10;</xsl:text>
	</xsl:for-each>		
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

</xsl:stylesheet>