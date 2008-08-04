<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>

	<xsl:template match="map">
		<contexts>
			<xsl:apply-templates select="*"/>
		</contexts>
	</xsl:template>

	<xsl:template match="mapID">
		<context>
			<xsl:attribute name="id">
                <xsl:call-template name="replace">
                    <xsl:with-param name="text" select="@target"/>
                </xsl:call-template>
			</xsl:attribute>
			<topic label="RIT" href="help/{@url}"/>
		</context>
	</xsl:template>

    <xsl:template name="replace">
        <xsl:param name="text"/>

        <xsl:choose>
            <xsl:when test="contains($text,'.')">
                <xsl:value-of select="substring-before($text,'.')"/>
                <xsl:text>_</xsl:text>
                <xsl:call-template name="replace">
                    <xsl:with-param name="text" select="substring-after($text,'.')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
