package com.marcos.conversordemoedas.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class HistoryOwnerServiceTest {

    private final HistoryOwnerService historyOwnerService = new HistoryOwnerService();

    @Test
    void keepsAnUnpredictableOwnerPerSession() {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        String firstOwner = historyOwnerService.getOwnerId(firstRequest);
        HttpSession firstSession = firstRequest.getSession(false);

        assertThat(UUID.fromString(firstOwner)).isNotNull();
        MockHttpServletRequest nextRequestInSameSession = new MockHttpServletRequest();
        nextRequestInSameSession.setSession(firstSession);
        assertThat(historyOwnerService.getOwnerId(nextRequestInSameSession)).isEqualTo(firstOwner);
        assertThat(firstSession).isNotNull();

        String secondOwner = historyOwnerService.getOwnerId(new MockHttpServletRequest());
        assertThat(secondOwner).isNotEqualTo(firstOwner);
    }
}
