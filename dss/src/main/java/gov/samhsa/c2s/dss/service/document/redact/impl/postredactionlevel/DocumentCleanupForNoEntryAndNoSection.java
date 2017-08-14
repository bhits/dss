package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.dss.config.DssProperties;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class DocumentCleanupForNoEntryAndNoSection extends AbstractPostRedactionLevelRedactionHandler {

    /**
     * The Constant URN_HL7_V3.
     */
    private static final String URN_HL7_V3 = "urn:hl7-org:v3";
    /**
     * The Constant TAG_COMPONENT.
     */
    private static final String TAG_COMPONENT = "component";
    /**
     * The Constant TAG_SECTION.
     */
    private static final String TAG_SECTION = "section";
    /**
     * The Constant XPATH_NO_COMPONENT_IN_STRUCTURED_BODY.
     */
    private static final String XPATH_NO_COMPONENT_IN_STRUCTURED_BODY = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody[not(hl7:component)]";
    /**
     * The Constant XPATH_SECTION_COMPONENT_WITH_NO_ENTRY.
     */
    private static final String XPATH_SECTION_COMPONENT_WITH_NO_ENTRY = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component[hl7:section[not(hl7:entry)]]";

    private final DssProperties dssProperties;

    /**
     * Instantiates a new document cleanup for no section.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public DocumentCleanupForNoEntryAndNoSection(DocumentAccessor documentAccessor, DssProperties dssProperties) {
        super(documentAccessor);
        this.dssProperties = dssProperties;
    }

    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                        RuleExecutionContainer ruleExecutionContainer, RedactionHandlerResult preRedactionResults,
                        PdpObligationsComplementSetDto pdpObligationsComplementSetDto, String documentType) {
        try {
            // Clean up section components with no entries
            cleanUpSectionComponentsWithNoEntries(xmlDocument, documentType);

            // Add empty component/section under structuredBody if none exists
            // (required to pass validation)
            addEmptySectionComponentIfNoneExists(xmlDocument);
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    /**
     * Adds the empty section component if none exists.
     *
     * @param xmlDocument the xml document
     * @throws DocumentAccessorException the document accessor exception
     */
    private void addEmptySectionComponentIfNoneExists(Document xmlDocument)
            throws DocumentAccessorException {
        final Optional<Node> structuredBody = documentAccessor.getNode(
                xmlDocument, XPATH_NO_COMPONENT_IN_STRUCTURED_BODY);
        structuredBody.ifPresent(sb -> {
            final Element emptyComponent = xmlDocument.createElementNS(
                    URN_HL7_V3, TAG_COMPONENT);
            final Element emptySection = xmlDocument.createElementNS(
                    URN_HL7_V3, TAG_SECTION);
            emptyComponent.appendChild(emptySection);
            sb.appendChild(emptyComponent);
        });
    }

    /**
     * Clean up section components with no entries.
     *
     * @param xmlDocument the xml document
     * @throws DocumentAccessorException the document accessor exception
     */
    private void cleanUpSectionComponentsWithNoEntries(Document xmlDocument, String documentType)
            throws DocumentAccessorException {
        // Find all empty sections
        final List<Node> emptySectionComponents = documentAccessor.getNodeListAsStream(
                xmlDocument, XPATH_SECTION_COMPONENT_WITH_NO_ENTRY).collect(toList());
        // Find the required sections out of the empty sections
        final List<Node> requiredButEmptySectionComponents = emptySectionComponents.stream()
                .filter(node -> isRequired(node, documentType))
                .collect(toList());
        // Remove the empty sections that are not required
        emptySectionComponents.stream()
                .filter(section -> !requiredButEmptySectionComponents.contains(section))
                .forEach(this::nullSafeRemove);
        // Set @nullFlavor=NI and add "No information" text on required but empty sections
        requiredButEmptySectionComponents.stream()
                .flatMap(this::componentToSection)
                .peek(section -> section.getAttributes().setNamedItem(createNullFlavorNIAttribute(xmlDocument)))
                .flatMap(this::sectionToText)
                .forEach(text -> {
                    nullSafeRemoveChildNodes(text);
                    Node textNode = createTextNode(xmlDocument, "No information");
                    text.appendChild(textNode);
                });
    }

    private Node createNullFlavorNIAttribute(Document xmlDocument) {
        final Attr nullFlavor = xmlDocument.createAttributeNS(URN_HL7_V3, "nullFlavor");
        nullFlavor.setValue("NI");
        return nullFlavor;
    }

    private Node createTextNode(Document xmlDocument, String data) {
        return xmlDocument.createTextNode(data);
    }

    private boolean isRequired(Node componentNode, String documentType) {
        return componentToSection(componentNode)
                .flatMap(this::sectionToCode)
                .map(code -> code.getAttributes().getNamedItem("code"))
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .filter(Objects::nonNull)
                .filter(code -> dssProperties.getRedact().getDocumentTypes().get(documentType).getRequiredSections().contains(code))
                .findAny().isPresent();
    }

    private Stream<Node> componentToSection(Node componentNode) {
        return getChildElementNodeWithName(componentNode, "section");
    }

    private Stream<Node> sectionToCode(Node sectionNode) {
        return getChildElementNodeWithName(sectionNode, "code");
    }

    private Stream<Node> sectionToText(Node sectionNode) {
        return getChildElementNodeWithName(sectionNode, "text");
    }

    private Stream<Node> getChildElementNodeWithName(Node node, String elementName) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .map(DocumentAccessor::toNodeStream)
                .orElseGet(Stream::empty)
                .filter(child -> Node.ELEMENT_NODE == child.getNodeType())
                .filter(child -> elementName.equals(child.getNodeName()));
    }
}
