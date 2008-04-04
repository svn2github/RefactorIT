<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent = "no"/>

<xsl:template match="/">
<xsl:apply-templates select="report"/>
</xsl:template>

<xsl:template match="report">
	<xsl:text>Package,Class,Member,Type</xsl:text>
	<xsl:apply-templates select="whole-types"/>
	<xsl:apply-templates select="single-members"/>
</xsl:template>

<xsl:template match="whole-types"> 
	<xsl:for-each select="/report/whole-types//descendant::*[not(child::*)]">
		<xsl:text>&#10;</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="ancestor::*[self::package]/attribute::name"/>
		</xsl:call-template>
		<xsl:text>,</xsl:text>
		
		<xsl:call-template name="escape">
			<xsl:with-param name="string">
				<xsl:call-template name="printWholeTypeName"/>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:text>,</xsl:text>	
		
		<xsl:text>,</xsl:text>
		
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="name()"/>
		</xsl:call-template>
	</xsl:for-each>
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
	<xsl:for-each select="/report/single-members//descendant::*[not(child::*)]">
		<xsl:text>&#10;</xsl:text>
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="ancestor::*[self::package]/attribute::name"/>
		</xsl:call-template>
		<xsl:text>,</xsl:text>
		
		<xsl:call-template name="escape">
			<xsl:with-param name="string">
				<xsl:call-template name="printFullClassName"/>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:text>,</xsl:text>
		
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="@name"/>
		</xsl:call-template>
		<xsl:text>,</xsl:text>
		
		<xsl:call-template name="escape">
			<xsl:with-param name="string" select="name()"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

    <xsl:template name="printFullClassName">
        <xsl:for-each select="ancestor::*[ancestor::package]">
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