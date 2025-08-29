package se.idpsim.Idpsimulator.service.saml;

import java.security.cert.X509Certificate;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;
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

        this.singleSignOnServiceUrl = singleSignOnServiceUrl;
        this.entityId = entityId;
        this.singleLogoutService = singleLogoutService;
        this.signingCertificate = signingCertificate;

        entityDescriptor = createSamlMetadata();
    }

    private EntityDescriptor createSamlMetadata() {
        XMLObjectBuilderFactory builderFactory =
            XMLObjectProviderRegistrySupport.getBuilderFactory();

        EntityDescriptor entityDescriptor =
            (EntityDescriptor) builderFactory.getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME)
                .buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);

        entityDescriptor.setEntityID(entityId);

        IDPSSODescriptor idpDescriptor =
            (IDPSSODescriptor) builderFactory.getBuilder(IDPSSODescriptor.DEFAULT_ELEMENT_NAME)
                .buildObject(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        idpDescriptor.setWantAuthnRequestsSigned(false);
        idpDescriptor.addSupportedProtocol("urn:oasis:names:tc:SAML:2.0:protocol");

        // Add SingleSignOnService
        SingleSignOnService ssoService = (SingleSignOnService) builderFactory.getBuilder(
                SingleSignOnService.DEFAULT_ELEMENT_NAME)
            .buildObject(SingleSignOnService.DEFAULT_ELEMENT_NAME);

        ssoService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        ssoService.setLocation("abc"); // endpoint
        idpDescriptor.getSingleSignOnServices()
            .add(ssoService);

        // Add NameIDFormat
        var nameIdFormat =
            (NameIDFormat) builderFactory.getBuilder(NameIDFormat.DEFAULT_ELEMENT_NAME)
                .buildObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
        nameIdFormat.setURI(NameIDType.TRANSIENT);
        idpDescriptor.getNameIDFormats()
            .add(nameIdFormat);

        KeyDescriptor keyDescriptor =
            (KeyDescriptor) builderFactory.getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME)
                .buildObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);

        keyDescriptor.setUse(UsageType.SIGNING);
        // Add X509Certificate
        // ... create KeyInfo, X509Data, X509Certificate ...

        idpDescriptor.getKeyDescriptors()
            .add(keyDescriptor);
        entityDescriptor.getRoleDescriptors()
            .add(idpDescriptor);
        return entityDescriptor;
    }
}
