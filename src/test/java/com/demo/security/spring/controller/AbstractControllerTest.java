package com.demo.security.spring.controller;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest {

    public String getTestUserName() {
        return this.getClass().getName().substring(0, 1).toLowerCase() + this.getClass().getName().substring(1);
    }

    public String getTestParam() {
        return "testParam=true";
    }

    /**
     * Common test steps for testing the security requirements of a resource for which the user must be logged
     * but for which there is no specific required role.
     * @param mockMvc
     * @param baseUrl
     * @throws Exception
     */
    public void testSecuredBaseUrlAuth(MockMvc mockMvc, String baseUrl) throws Exception {
        final String testUserName = getTestUserName();
        mockMvc.perform(get(baseUrl)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(baseUrl + "/")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam())).andExpect(status().isUnauthorized());
        mockMvc.perform(get(baseUrl).with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/").with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam()).with(user(testUserName))).andExpect(status().isOk());
    }

    /**
     * Common test steps for testing the security requirements of a resource for which the user does NOT have to be logged in
     * @param mockMvc
     * @param baseUrl
     * @throws Exception
     */
    public void testUnSecuredBaseUrlAuth(MockMvc mockMvc, String baseUrl) throws Exception {
        final String testUserName = getTestUserName();
        mockMvc.perform(get(baseUrl)).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/")).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam())).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl).with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/").with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam()).with(user(testUserName))).andExpect(status().isOk());
    }
}
