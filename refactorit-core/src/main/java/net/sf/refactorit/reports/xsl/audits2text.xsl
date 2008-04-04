<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent = "no"/>

<xsl:variable name = "violations" select="/report//violation"/>
<xsl:variable name = "audits" select="/report/info/audit[@id = $violations/@audit]"/>
<xsl:variable name = "categories" select="/report/info/audit[not(@category = preceding-sibling::audit/@category)]"/>
<xsl:variable name="notEmptyCategories" select="$categories[@category = $audits/@category]"/>

<xsl:template match="/">
  <xsl:apply-templates select="report"/>
</xsl:template>

<xsl:template match="report">
<xsl:for-each select="$audits">
	<xsl:variable name="currentAudit" select="current()"/>
	
	<xsl:value-of select="@name"/><xsl:text> &#9; </xsl:text>
	<xsl:text>Corrective Action</xsl:text><xsl:text> &#9; </xsl:text>
	<xsl:text>Location</xsl:text><xsl:text> &#9; </xsl:text>
	<xsl:text>Line</xsl:text><xsl:text> &#9; </xsl:text>
	<xsl:text>Density</xsl:text><xsl:text> &#9; </xsl:text>
	<xsl:text>Priority</xsl:text><xsl:text>&#10;</xsl:text>

 	<xsl:for-each select="$violations[@audit = $currentAudit/@id]">
		<xsl:value-of select="@message"/><xsl:text> &#9; </xsl:text>
		
		<xsl:choose>
			<xsl:when test="count(corrective-action) = 0">
				<xsl:text>    -   &#9; </xsl:text>
			</xsl:when>
			<xsl:otherwise>
		      	<xsl:for-each select="corrective-action">
		      		<xsl:if test="count(parent::node()/corrective-action) > 1">
		          		<xsl:value-of select="position()"/><xsl:text>. </xsl:text>
		      		</xsl:if>
		      		<xsl:value-of select="@name"/>
		      		<xsl:if test="position() != last()">
						<xsl:text>&#32;</xsl:text>
		      		</xsl:if>
		      	</xsl:for-each>
	    		<xsl:text> &#9; </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
      	
		<xsl:call-template name="printLocation"/><xsl:text> &#9; </xsl:text>
		
		<xsl:value-of select="@line"/><xsl:text> &#9; </xsl:text>
		
		<xsl:value-of select="@density"/><xsl:text> &#9; </xsl:text>
		
		<xsl:value-of select="$currentAudit/@priority"/><xsl:text>&#10;</xsl:text>
		</xsl:for-each>
	</xsl:for-each>
</xsl:template>

         <xsl:template name="printLocation">
            <xsl:for-each select="ancestor::*[ancestor::report][name() != 'children']">
            			<xsl:value-of select="@name"/>
                        <xsl:if test="position() != last()">
                                <xsl:text>.</xsl:text>
                        </xsl:if>
            </xsl:for-each>
        </xsl:template>


</xsl:stylesheet>
