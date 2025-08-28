package se.idpsim.Idpsimulator.service.saml;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
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
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import se.idpsim.Idpsimulator.utils.ObjectUtils;

@Getter
public class SamlResponse {

    private String issuer; //the IdP issuer
    private String destination; //the service providers ACS URL
    private String inResponseTo; //the ID of the SamlRequest
    private List<SamlAssertion> assertions;
    private String nameId; //The NameID to use in the response
    private Response response;
    private Instant createdAt;
    private String audience; //The issuer of the SamlRequest

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

    private void buildSamlResponse() {
        var builderFactory = SamlUtils.getXmlObjectBuilderFactory();

        //we create Issuer two times, otherwise OpenSaml complains
        Issuer issuer = createIssuer(builderFactory);
        Issuer issuer1 = createIssuer(builderFactory);

        Subject subject = createSubject(builderFactory);
        Status status = createStatus(builderFactory);
        Conditions conditions = createConditions(builderFactory);
        AttributeStatement attributeStatement = createAttributeStatement(builderFactory);

        Assertion assertion = createAssertion(builderFactory);
        assertion.setIssuer(issuer1);
        assertion.setSubject(subject);
        assertion.setConditions(conditions);
        assertion.getAttributeStatements()
            .add(attributeStatement);

        response = (Response) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME)
            .buildObject(Response.DEFAULT_ELEMENT_NAME);
        response.setIssuer(issuer);
        response.setInResponseTo(this.inResponseTo);
        response.setID(UUID.randomUUID()
            .toString());
        response.setDestination(destination);
        response.setStatus(status);
        response.getAssertions()
            .add(assertion);
    }

    private Instant getNotOnOrAfter() {
        return createdAt.plusSeconds(300);
    }

    private Conditions createConditions(XMLObjectBuilderFactory builderFactory) {
        Conditions conditions = (Conditions) builderFactory.getBuilder(Conditions.DEFAULT_ELEMENT_NAME)
            .buildObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(createdAt);
        conditions.setNotOnOrAfter(getNotOnOrAfter());

        // Add AudienceRestriction
        var audienceRestriction = (AudienceRestriction) builderFactory.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME)
            .buildObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);

        var audience = (Audience) builderFactory.getBuilder(Audience.DEFAULT_ELEMENT_NAME).buildObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setURI(this.audience);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AttributeStatement createAttributeStatement(XMLObjectBuilderFactory builderFactory) {
        var attributeStatement =
            (AttributeStatement) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME)
                .buildObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        for (SamlAssertion samlAssertion : assertions) {
            Attribute attribute =
                (Attribute) builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME)
                    .buildObject(Attribute.DEFAULT_ELEMENT_NAME);

            attribute.setName(samlAssertion.getName());
            attribute.setFriendlyName(samlAssertion.getFriendlyName());

            var attrVal = (XSString) builderFactory.getBuilder(XSString.TYPE_NAME)
                .buildObject(XSString.TYPE_NAME);
            attrVal.setValue(samlAssertion.getValue());
            attribute.getAttributeValues()
                .add(attrVal);
            attributeStatement.getAttributes()
                .add(attribute);
        }
        return attributeStatement;
    }

    private Assertion createAssertion(XMLObjectBuilderFactory builderFactory) {
        Assertion assertion = (Assertion) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME)
            .buildObject(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID("_" + UUID.randomUUID());
        assertion.setIssueInstant(createdAt);
        return assertion;
    }

    private Issuer createIssuer(XMLObjectBuilderFactory builderFactory) {
        var issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        var issuer = issuerBuilder.buildObject();
        issuer.setValue(this.issuer);
        return issuer;
    }

    private NameID createNameId(XMLObjectBuilderFactory builderFactory) {
        var nameIdObject = (NameID) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)
            .buildObject(NameID.DEFAULT_ELEMENT_NAME);

        nameIdObject.setFormat(NameID.UNSPECIFIED);
        nameIdObject.setValue(this.nameId);
        return nameIdObject;
    }

    private SubjectConfirmationData createSubjectConfirmationData(
        XMLObjectBuilderFactory builderFactory) {
        SubjectConfirmationData confirmationData =
            (SubjectConfirmationData) builderFactory.getBuilder(
                    SubjectConfirmationData.DEFAULT_ELEMENT_NAME)
                .buildObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        confirmationData.setRecipient(destination);
        confirmationData.setInResponseTo(inResponseTo);
        confirmationData.setNotOnOrAfter(getNotOnOrAfter());

        return confirmationData;
    }

    private SubjectConfirmation createSubjectConfirmation(XMLObjectBuilderFactory builderFactory) {
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) builderFactory.getBuilder(
                SubjectConfirmation.DEFAULT_ELEMENT_NAME)
            .buildObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData confirmationData = createSubjectConfirmationData(builderFactory);
        subjectConfirmation.setSubjectConfirmationData(confirmationData);
        return subjectConfirmation;
    }

    private Subject createSubject(XMLObjectBuilderFactory builderFactory) {
        Subject subject = (Subject) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME)
            .buildObject(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = createNameId(builderFactory);
        subject.setNameID(nameID);
        SubjectConfirmation subjectConfirmation = createSubjectConfirmation(builderFactory);
        subject.getSubjectConfirmations()
            .add(subjectConfirmation);

        nameID = createNameId(builderFactory);
        subject.setNameID(nameID);
        return subject;
    }

    private Status createStatus(XMLObjectBuilderFactory builderFactory) {
        StatusCode statusCode =
            (StatusCode) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME)
                .buildObject(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(StatusCode.SUCCESS);

        Status status = (Status) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME)
            .buildObject(Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode(statusCode);
        return status;
    }
}
