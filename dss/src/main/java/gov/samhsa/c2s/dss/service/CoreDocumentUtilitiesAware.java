package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;

public interface CoreDocumentUtilitiesAware {

    DocumentXmlConverter getDocumentXmlConverter();

    DocumentAccessor getDocumentAccessor();
}
