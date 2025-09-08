package se.idpsim.Idpsimulator.service.saml;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.xml.transform.TransformerException;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import se.idpsim.Idpsimulator.utils.ObjectUtils;


public class SamlMetadata {

    @Getter
    private String singleSignOnServiceUrl;

    @Getter
    private String entityId;

    @Getter
    private String singleLogoutService;

    @Getter
    private X509Certificate signingCertificate;


    private EntityDescriptor entityDescriptor;

    EntityDescriptor getEntityDescriptor() {
        return entityDescriptor;
    }

    @Builder
    public SamlMetadata(String singleSignOnServiceUrl, String entityId, String singleLogoutService,
        X509Certificate signingCertificate) {

        ObjectUtils.requireNonEmpty(singleSignOnServiceUrl,
            "singleSignOnServiceUrl cannot be empty");
        ObjectUtils.requireNonEmpty(entityId, "entityId cannot be empty");
        ObjectUtils.requireNonEmpty(singleLogoutService, "singleLogoutService cannot be empty");
        ObjectUtils.requireNonNull(signingCertificate, "signingCertificate cannot be null");

        this.singleSignOnServiceUrl = singleSignOnServiceUrl;
        this.entityId = entityId;
        this.singleLogoutService = singleLogoutService;
        this.signingCertificate = signingCertificate;

        try {
            entityDescriptor = createSamlMetadata();
        } catch(CertificateEncodingException e) {
            //TODO fix better exception
            throw new RuntimeException(e);
        }
    }

    private EntityDescriptor createSamlMetadata() throws CertificateEncodingException {
        EntityDescriptor entityDescriptor = SamlUtils.createObject(
            EntityDescriptor.class, EntityDescriptor.DEFAULT_ELEMENT_NAME);

        entityDescriptor.setEntityID(entityId);

        IDPSSODescriptor idpDescriptor = SamlUtils.createObject(
            IDPSSODescriptor.class, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        idpDescriptor.setWantAuthnRequestsSigned(false);
        idpDescriptor.addSupportedProtocol("urn:oasis:names:tc:SAML:2.0:protocol");

        // Add SingleSignOnService
        SingleSignOnService ssoService = SamlUtils.createObject(
            SingleSignOnService.class, SingleSignOnService.DEFAULT_ELEMENT_NAME);

        ssoService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        ssoService.setLocation(singleSignOnServiceUrl);
        idpDescriptor.getSingleSignOnServices()
            .add(ssoService);

        // Add NameIDFormat
        var nameIdFormat = SamlUtils.createObject(
            NameIDFormat.class, NameIDFormat.DEFAULT_ELEMENT_NAME);
        nameIdFormat.setURI(NameIDType.TRANSIENT);
        idpDescriptor.getNameIDFormats()
            .add(nameIdFormat);

        //set keydescriptor
        KeyDescriptor keyDescriptor = SamlUtils.createObject(
            KeyDescriptor.class, KeyDescriptor.DEFAULT_ELEMENT_NAME);
        keyDescriptor.setUse(UsageType.SIGNING);
        KeyInfo keyInfo = SamlUtils.createObject(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = SamlUtils.createObject(X509Data.class, X509Data.DEFAULT_ELEMENT_NAME);
        org.opensaml.xmlsec.signature.X509Certificate x509CertElement = SamlUtils.createObject(
            org.opensaml.xmlsec.signature.X509Certificate.class,
            org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME);

        String encodedCert = Base64.getEncoder().encodeToString(signingCertificate.getEncoded());
        x509CertElement.setValue(encodedCert);
        x509Data.getX509Certificates().add(x509CertElement);
        keyInfo.getX509Datas().add(x509Data);
        keyDescriptor.setKeyInfo(keyInfo);

        idpDescriptor.getKeyDescriptors()
            .add(keyDescriptor);
        entityDescriptor.getRoleDescriptors()
            .add(idpDescriptor);
        return entityDescriptor;
    }

    //TODO better exception handling
    public String toString() {
        try {
            var doc = SamlUtils.toDocument(entityDescriptor);
            return SamlUtils.documentToString(doc);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
