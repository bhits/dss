<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="xs" version="2.0"
    xpath-default-namespace="http://hl7.org/fhir"
    xmlns:fhir="http://hl7.org/fhir">
    <xsl:output indent="yes" omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- Extracts clinical facts from the original FHIR Bundle. A clinical fact contains a code, a displayName, a code system,
    the title and LOINC code of the section that contains the fact, and the observationId. The fact model also includes the XACML Result from the PDP.
    The fact model is inserted into the working memory of the Drools rules engine. The latter returns a set of directives for segmenting the FHIR Bundle.-->

    <xsl:variable name="xacmlResult" select="document('xacmlResult')"/>

    <xsl:template match="/">
        <FactModel xsl:exclude-result-prefixes="#all">
            <xsl:sequence select="$xacmlResult"/>

            <xsl:variable name="entry"
                select="/Bundle/entry"/>

            <ClinicalFacts>
                
                <xsl:for-each select="$entry//coding[code[@value] and system[@value]]">
                    <ClinicalFact>
                        <xsl:call-template name="clinicalFacts"/>
                    </ClinicalFact>
                </xsl:for-each>

            </ClinicalFacts>

            <EntryReferences>

                <xsl:for-each select="$entry//reference">
                    <EntryReference>
                        <xsl:call-template name="entryReferences"/>
                    </EntryReference>
                </xsl:for-each>

            </EntryReferences>

            <EmbeddedFHIRBundle xsl:exclude-result-prefixes="#all" xmlns="http://hl7.org/fhir">
                <xsl:call-template name="FHIRBundle"/>
                <xsl:call-template name="entry"/>
            </EmbeddedFHIRBundle>
        </FactModel>

    </xsl:template>
    
    <xsl:template name="clinicalFacts" exclude-result-prefixes="#all">
        <code>
            <xsl:value-of select="code/@value"/>
        </code>
        <displayName>
            <xsl:value-of select="display/@value"/>
        </displayName>
        <codeSystem>
            <xsl:value-of select="replace(system/@value, 'urn:oid:', '')"/>
        </codeSystem>
        <entry>
            <xsl:value-of select="generate-id(ancestor::entry)"/>
        </entry>
    </xsl:template>

    <xsl:template name="entryReferences" exclude-result-prefixes="#all">
        <entry>
            <xsl:value-of select="generate-id(ancestor::entry)"/>
        </entry>
        <reference>
            <xsl:value-of select="replace(@value, '#', '')"/>
        </reference>
    </xsl:template>

    <xsl:template name="FHIRBundle" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="entry" match="entry">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <generatedEntryId xsl:exclude-result-prefixes="#all" xmlns="http://hl7.org/fhir">
                <xsl:value-of select="generate-id(.)"/>
            </generatedEntryId>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
