package se.idpsim.Idpsimulator.service.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class SamlUtils {

    private static final DocumentBuilderFactory documentBuilderFactory =
        DocumentBuilderFactory.newInstance();

    private static final RandomIdentifierGenerationStrategy secureRandomIdGenerator =
        new RandomIdentifierGenerationStrategy();

    static {
        init();
    }

    static void init() {
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

    static XMLObjectBuilderFactory getXmlObjectBuilderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

    static String samlResponseToString(SamlResponse samlResponse)
        throws MarshallingException, TransformerException {
        var doc = toDocument(samlResponse.getResponse());
        return documentToString(doc);
    }

    static Document toDocument(XMLObject xmlObject)
        throws MarshallingException, TransformerException {
        MarshallerFactory marshallerFactory =
            XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        marshaller.marshall(xmlObject);
        Element element = marshaller.marshall(xmlObject);
        return element.getOwnerDocument();
    }

    static String documentToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    static String generateSecureRandomId() {
        return secureRandomIdGenerator.generateIdentifier();
    }

    static Signature createSignature(BasicX509Credential credential,
        String signatureAlgorithm, String canonicalizationMethod) {
        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
            .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
            .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setCanonicalizationAlgorithm(canonicalizationMethod);
        return signature;
    }

}
