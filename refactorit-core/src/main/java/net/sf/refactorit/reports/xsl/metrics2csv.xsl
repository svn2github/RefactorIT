<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent = "no"/>

<xsl:template match="/">
  <xsl:apply-templates select="report"/>
</xsl:template>

<xsl:template match="report">
	<xsl:text>Location</xsl:text><xsl:text>,</xsl:text>
	<xsl:text>Type</xsl:text><xsl:text>,</xsl:text>
	
	<xsl:for-each select="info/metric">
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="@name"/>
		</xsl:call-template>
		<xsl:text>,</xsl:text>
	</xsl:for-each>
	<xsl:text>&#10;</xsl:text>

	<xsl:for-each select="descendant::*[count(child::metric) > 0][name() != 'info']">
			<xsl:variable name="current" select="."/>
			
			<xsl:call-template name="escape">
				<xsl:with-param name="string">
					<xsl:call-template name="printLocation"/>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:text>,</xsl:text>
						
			<xsl:call-template name="escape">
				<xsl:with-param name="string" select="name()"/>
			</xsl:call-template>			
			<xsl:text>,</xsl:text>
			
			<xsl:for-each select="/report/info/metric">
				<xsl:choose> 
					<xsl:when test="$current/metric[@name = current()/@name]"> 
						<xsl:variable name="value" select="$current/metric[@name = current()/@name]/@value"/>
						<xsl:value-of select="format-number($value,'#.###')"/><xsl:text>,</xsl:text>
					</xsl:when> 
					<xsl:otherwise> 
						<xsl:text>,</xsl:text>
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
    
    
    <!-- escaping and replacing functions here! -->
	 <xsl:template name="replace">
		<xsl:param name="string" select="''"/>
		<xsl:param name="pattern" select="''"/>
		<xsl:param name="replacement" select="''"/>
		<xsl:choose>
			<xsl:when test="$pattern != '' and $string != '' and contains($string, $pattern)">
				<xsl:value-of select="substring-before($string, $pattern)"/>
					<!--
					Use "xsl:copy-of" instead of "xsl:value-of" so that users
					may substitute nodes as well as strings for $replacement.
					-->
				<xsl:copy-of select="$replacement"/>
				<xsl:call-template name="replace">
					<xsl:with-param name="string" select="substring-after($string, $pattern)"/>
					<xsl:with-param name="pattern" select="$pattern"/>
					<xsl:with-param name="replacement" select="$replacement"/>
		    	</xsl:call-template>
			</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$string"/>
		</xsl:otherwise>
		</xsl:choose>
     </xsl:template>
     
     <xsl:template name="escape">
     	<xsl:param name="string" select="''"/>
     	<xsl:choose>
     		<xsl:when test="$string != '' and ( contains($string, ',') or contains($string, '&#34;') )">
				<xsl:text>"</xsl:text>
				<xsl:call-template name="replace">
					<xsl:with-param name="string" select="$string"/>
					<xsl:with-param name="pattern" select="'&#34;'"/>
					<xsl:with-param name="replacement" select="'&#34;&#34;'"/>
				</xsl:call-template>
				<xsl:text>"</xsl:text>
     		</xsl:when>
     		<xsl:otherwise>
				<xsl:value-of select="$string"/>
     		</xsl:otherwise>
     	</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>