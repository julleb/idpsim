package se.idpsim.Idpsimulator.service.saml;

import java.time.Instant;
import java.util.List;
import javax.xml.transform.TransformerException;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import se.idpsim.Idpsimulator.utils.ObjectUtils;


public class SamlResponse {

    @Getter
    private String issuer; //the IdP issuer

    @Getter
    private String destination; //the service providers ACS URL

    @Getter
    private String inResponseTo; //the ID of the SamlRequest

    @Getter
    private List<SamlAssertion> assertions;

    @Getter
    private String nameId; //The NameID to use in the response

    private Response response;
    private Instant createdAt;

    @Getter
    private String audience; //The issuer of the SamlRequest

    Response getResponse() {
        return response;
    }

    @Builder
    public SamlResponse(String issuer, String destination, String inResponseTo,
        List<SamlAssertion> assertions, String nameId, String audience) {

        ObjectUtils.requireNonEmpty(issuer, "issuer cannot be empty");
        ObjectUtils.requireNonEmpty(destination, "destination cannot be empty");
        ObjectUtils.requireNonEmpty(inResponseTo, "inResponseTo cannot be empty");
        ObjectUtils.requireNonEmpty(assertions, "assertions cannot be empty");
        ObjectUtils.requireNonEmpty(nameId, "nameId cannot be empty");
        ObjectUtils.requireNonEmpty(audience, "audience cannot be empty");


        this.issuer = issuer;
        this.destination = destination;
        this.inResponseTo = inResponseTo;
        this.assertions = assertions;
        this.nameId = nameId;
        this.audience = audience;
        createdAt = Instant.now();
        buildSamlResponse();
    }

    public String toString() {
        try {
            return SamlUtils.samlResponseToString(this);
        } catch (MarshallingException e) {
            throw new RuntimeException(e); //TODO better exception
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildSamlResponse() {
        //we create Issuer two times, otherwise OpenSaml complains
        Issuer issuer = createIssuer();
        Issuer issuer1 = createIssuer();

        Subject subject = createSubject();
        Status status = createStatus();
        Conditions conditions = createConditions();
        AttributeStatement attributeStatement = createAttributeStatement();

        Assertion assertion = createAssertion();
        assertion.setIssuer(issuer1);
        assertion.setSubject(subject);
        assertion.setConditions(conditions);
        assertion.getAttributeStatements()
            .add(attributeStatement);

        response = SamlUtils.createObject(Response.class, Response.DEFAULT_ELEMENT_NAME);
        response.setIssuer(issuer);
        response.setInResponseTo(this.inResponseTo);
        response.setID(SamlUtils.generateSecureRandomId());
        response.setDestination(destination);
        response.setStatus(status);
        response.getAssertions()
            .add(assertion);
    }

    private Instant getNotOnOrAfter() {
        return createdAt.plusSeconds(300);
    }

    private Conditions createConditions() {
        Conditions conditions = SamlUtils.createObject(Conditions.class,
            Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(createdAt);
        conditions.setNotOnOrAfter(getNotOnOrAfter());

        // Add AudienceRestriction
        var audienceRestriction = (SamlUtils.createObject(AudienceRestriction.class,
            AudienceRestriction.DEFAULT_ELEMENT_NAME));

        var audience = (SamlUtils.createObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME));
        audience.setURI(this.audience);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AttributeStatement createAttributeStatement() {
        var attributeStatement = SamlUtils.createObject(AttributeStatement.class,
            AttributeStatement.DEFAULT_ELEMENT_NAME);
        for (SamlAssertion samlAssertion : assertions) {
            Attribute attribute = SamlUtils.createObject(Attribute.class,
                Attribute.DEFAULT_ELEMENT_NAME);
            attribute.setName(samlAssertion.getName());
            attribute.setFriendlyName(samlAssertion.getFriendlyName());

            var attrVal = SamlUtils.createObject(XSString.class, XSString.TYPE_NAME);
            attrVal.setValue(samlAssertion.getValue());
            attribute.getAttributeValues()
                .add(attrVal);
            attributeStatement.getAttributes()
                .add(attribute);
        }
        return attributeStatement;
    }

    private Assertion createAssertion() {
        Assertion assertion = SamlUtils.createObject(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID("_" + SamlUtils.generateSecureRandomId());
        assertion.setIssueInstant(createdAt);
        return assertion;
    }

    private Issuer createIssuer() {
        var issuer = SamlUtils.createObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(this.issuer);
        return issuer;
    }

    private NameID createNameId() {
        var nameIdObject = SamlUtils.createObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameIdObject.setFormat(NameID.UNSPECIFIED);
        nameIdObject.setValue(this.nameId);
        return nameIdObject;
    }

    private SubjectConfirmationData createSubjectConfirmationData() {
        SubjectConfirmationData confirmationData = SamlUtils.createObject(SubjectConfirmationData.class,
            SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        confirmationData.setRecipient(destination);
        confirmationData.setInResponseTo(inResponseTo);
        confirmationData.setNotOnOrAfter(getNotOnOrAfter());

        return confirmationData;
    }

    private SubjectConfirmation createSubjectConfirmation() {
        SubjectConfirmation subjectConfirmation = SamlUtils.createObject(SubjectConfirmation.class,
            SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData confirmationData = createSubjectConfirmationData();
        subjectConfirmation.setSubjectConfirmationData(confirmationData);
        return subjectConfirmation;
    }

    private Subject createSubject() {
        Subject subject = SamlUtils.createObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = createNameId();
        subject.setNameID(nameID);
        SubjectConfirmation subjectConfirmation = createSubjectConfirmation();
        subject.getSubjectConfirmations()
            .add(subjectConfirmation);

        nameID = createNameId();
        subject.setNameID(nameID);
        return subject;
    }

    private Status createStatus() {
        StatusCode statusCode = SamlUtils.createObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(StatusCode.SUCCESS);
        Status status = SamlUtils.createObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode(statusCode);
        return status;
    }
}
