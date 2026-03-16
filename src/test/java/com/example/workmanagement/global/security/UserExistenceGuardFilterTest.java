package com.example.workmanagement.global.security;

import com.example.workmanagement.domain.user.repository.UserRepository;
import com.example.workmanagement.global.security.filter.UserExistenceGuardFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserExistenceGuardFilterTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserExistenceGuardFilter filter = new UserExistenceGuardFilter(userRepository, new ObjectMapper());

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void existingUser_shouldPass() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("7", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.existsById(7L)).thenReturn(true);

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verify(userRepository).existsById(7L);
    }

    @Test
    void deletedUser_shouldReturn401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("8", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.existsById(8L)).thenReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        JsonNode root = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals("USER_NOT_FOUND", root.path("errorInfo").path("code").asText());
    }

    @Test
    void invalidSubject_shouldReturn401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("not-number", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        JsonNode root = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals("USER_INVALID_AUTH_SUBJECT", root.path("errorInfo").path("code").asText());
    }
}
