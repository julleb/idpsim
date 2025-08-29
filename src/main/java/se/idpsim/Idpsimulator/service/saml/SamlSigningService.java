package se.idpsim.Idpsimulator.service.saml;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.springframework.stereotype.Service;
import se.idpsim.Idpsimulator.utils.KeystoreUtils;

@Service
public class SamlSigningService {

    static final String keyStorePath = "classpath: saml/saml-keys.jks";
    static final String keyStoreAlias = "signing-key";
    static final String passwordToAlias = "lol";

    private final X509Certificate signingCertificate;
    private final PrivateKey signingPrivateKey;

    SamlSigningService() throws Exception {
        String keystorePassword = "";
        var keyStore = KeystoreUtils.getKeyStore(keyStorePath, keystorePassword);
        signingCertificate = KeystoreUtils.getAsX509Certificate(keyStore, keyStoreAlias);
        signingPrivateKey = KeystoreUtils.getPrivateKey(keyStore, keyStoreAlias, passwordToAlias);
    }


    public void signSamlResponse(SamlResponse samlResponse) {
        BasicX509Credential signingCredential = new BasicX509Credential(signingCertificate, signingPrivateKey);
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        String canonicalization  = SignatureConstants.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
        Signature signature = SamlUtils.createSignature(signingCredential, signatureAlgorithm, canonicalization);

        samlResponse.getResponse().setSignature(signature);
        SignatureSigningParameters signingParameters = new SignatureSigningParameters();
        signingParameters.setSigningCredential(signingCredential);
        signingParameters.setSignatureAlgorithm(signatureAlgorithm);
        signingParameters.setSignatureCanonicalizationAlgorithm(canonicalization);
        try {
            SignatureSupport.signObject(samlResponse.getResponse(), signingParameters);
        } catch (SecurityException e) {
            //TODO throw better exceptions
            throw new RuntimeException(e);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }


}
