package se.idpsim.Idpsimulator.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import se.idpsim.Idpsimulator.TestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DummySimulatorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getMetadata_shouldReturnOk() {
        webTestClient.get()
            .uri("/dummysimulator/metadata/0")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.TEXT_XML);
    }

    @Test
    void createSamlResponseForm_shouldThrowError_whenSamlResponseNotExistInSession() {
        webTestClient.post()
            .uri("/dummysimulator/resp/0")
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }

    @Test
    void createSamlResponseForm_shouldReturnOk_whenSamlResponseExistInSession() {
        var result = webTestClient.post()
            .uri("/dummysimulator/req/0")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("SAMLRequest",
                TestData.ENCODED_SAML_REQUEST_FROM_SKATTEVERKET))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Void.class)
            .returnResult();

        String sessionCookieName = "IDPSIM_SESSION";
        var sessionCookie = result.getResponseCookies()
            .getFirst(sessionCookieName);

        webTestClient.post()
            .uri("/dummysimulator/resp/0")
            .cookie(sessionCookie.getName(), sessionCookie.getValue())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("userId", "id123")
                    .with("firstName", "Alice")
                    .with("lastName", "Alisson")
                    .with("userAction", "SUBMIT")
            )
            .exchange()
            .expectStatus()
            .is2xxSuccessful();
    }

}
