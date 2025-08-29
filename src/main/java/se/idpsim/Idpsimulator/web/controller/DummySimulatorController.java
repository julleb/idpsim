package se.idpsim.Idpsimulator.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import se.idpsim.Idpsimulator.service.idp.DummySimulator;
import se.idpsim.Idpsimulator.utils.HttpServletRequestUtils;

@Controller
@Slf4j
@AllArgsConstructor
public class DummySimulatorController {

    private final DummySimulator dummySimulator;

    @GetMapping(value=DummySimulator.ENTITY_ID_SUFFIX, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> getMetadata() {
        String hostUrl = HttpServletRequestUtils.getHostUrl();
        return ResponseEntity.ok().body(dummySimulator.getSamlMetadata(hostUrl));
    }
}
