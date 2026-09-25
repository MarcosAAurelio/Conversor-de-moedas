package com.marcos.conversordemoedas.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HistoryOwnerService {

    private static final String OWNER_ATTRIBUTE = HistoryOwnerService.class.getName() + ".ownerId";

    public String getOwnerId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object currentOwnerId = session.getAttribute(OWNER_ATTRIBUTE);
        if (currentOwnerId instanceof String ownerId && !ownerId.isBlank()) {
            return ownerId;
        }

        String ownerId = UUID.randomUUID().toString();
        session.setAttribute(OWNER_ATTRIBUTE, ownerId);
        return ownerId;
    }
}
