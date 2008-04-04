<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent = "no"/>

<xsl:template match="/">
<xsl:apply-templates select="report"/>
</xsl:template>

<xsl:template match="report">
	<xsl:text>Package &#9; Class &#9; Member &#9; Type</xsl:text>
	<xsl:apply-templates select="whole-types"/>
	<xsl:apply-templates select="single-members"/>
</xsl:template>

<xsl:template match="whole-types"> 
	<xsl:for-each select="/report/whole-types//descendant::*[not(child::*)]">
		<xsl:text>&#10;</xsl:text>
		<xsl:value-of select="ancestor::*[self::package]/attribute::name"/><xsl:text> &#9; </xsl:text>
		<xsl:call-template name="printWholeTypeName"/><xsl:text> &#9; </xsl:text>
		<xsl:text>    -   &#9; </xsl:text>
		<xsl:value-of select="name()"/>	
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
		<xsl:value-of select="ancestor::*[self::package]/attribute::name"/><xsl:text> &#9; </xsl:text>
		<xsl:call-template name="printFullClassName"/><xsl:text> &#9; </xsl:text>
		<xsl:value-of select="@name"/><xsl:text> &#9; </xsl:text>
		<xsl:value-of select="name()"/>
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

</xsl:stylesheet>