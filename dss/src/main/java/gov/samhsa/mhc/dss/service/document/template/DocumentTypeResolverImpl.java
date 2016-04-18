package gov.samhsa.mhc.dss.service.document.template;


import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DocumentTypeResolverImpl implements DocumentTypeResolver {

    private static final String XPATH_TEMPLATE_ID = "/hl7:ClinicalDocument/hl7:templateId[@root]";

    @Autowired
    private DocumentAccessor documentAccessor;

    @Override
    public DocumentType resolve(Document document) {
        try {
            final Stream<Node> templateIds = documentAccessor.getNodeListAsStream(document, XPATH_TEMPLATE_ID);
            return templateIds
                    .map(node -> DocumentType.from(toRoot(node), toExtension(node)))
                    .filter(DocumentType::isIdentified)
                    .findAny()
                    .orElse(DocumentType.UNIDENTIFIED);
        } catch (DocumentAccessorException e) {
            throw new DocumentSegmentationException(e);
        }
    }

    private String toRoot(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getAttributes)
                .map(attributes -> attributes.getNamedItem("root"))
                .map(Node::getNodeValue)
                .filter(StringUtils::hasText)
                .orElseThrow(TemplateIdRootNotFoundException::new);
    }

    private Optional<String> toExtension(Node node) {
        return Optional.ofNullable(node)
                .map(Node::getAttributes)
                .map(attributes -> attributes.getNamedItem("extension"))
                .map(Node::getNodeValue)
                .filter(StringUtils::hasText);
    }
}
