<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>

	<xsl:param name="jhm"/>

	<xsl:variable name="map" select="document($jhm)"/>

	<xsl:template match="/">
		<xsl:apply-templates select="toc/*" mode="toc"/>
	</xsl:template>

	<xsl:template match="tocitem" mode="toc">
		<toc id="refactorit" label="{@text}">
			<xsl:attribute name="topic">
				<xsl:call-template name="map"/>
			</xsl:attribute>
			<xsl:apply-templates select="*"/>
		</toc>
	</xsl:template>

	<xsl:template match="tocitem">
		<topic label="{@text}">
			<xsl:attribute name="href">
				<xsl:call-template name="map"/>
			</xsl:attribute>
			<xsl:apply-templates select="*"/>
		</topic>
	</xsl:template>

	<xsl:template name="map">
		<xsl:variable name="t" select="@target"/>
		<xsl:text>help/</xsl:text>
		<xsl:value-of select="$map/map/mapID[@target=$t]/@url"/>
	</xsl:template>

</xsl:stylesheet>
