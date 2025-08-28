package se.idpsim.Idpsimulator.service.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;

class SamlUtils {

    private static final DocumentBuilderFactory documentBuilderFactory =
        DocumentBuilderFactory.newInstance();

    static {
        documentBuilderFactory.setNamespaceAware(true);
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Could not initialize OpenSaml", e);
        }
    }

    static byte[] decodeSamlRequest(String encodedSamlRequest) {
        return Base64.getDecoder()
            .decode(encodedSamlRequest);
    }

    static XMLObject toXmlObject(byte[] samlRequest)
        throws ParserConfigurationException, IOException, SAXException, UnmarshallingException {

        var documentBuilder = documentBuilderFactory.newDocumentBuilder();
        var document = documentBuilder.parse(new ByteArrayInputStream(samlRequest));
        var rootElement = document.getDocumentElement();

        var factory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
        var unmarshaller = factory.getUnmarshaller(rootElement);
        return unmarshaller.unmarshall(rootElement);
    }

}
