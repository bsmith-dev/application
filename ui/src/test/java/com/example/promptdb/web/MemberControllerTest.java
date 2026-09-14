package com.example.promptdb.web;

import com.example.promptdb.api.MemberApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class MemberControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MemberApiClient memberApiClient;

    @DisplayName("List renders members")
    @Test
    void listRendersMembers() throws Exception {
        List<?> members = List.of(ALICE, BOB);
        given(memberApiClient.listMembers()).willReturn(List.of(ALICE, BOB));

        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attribute("members", members));
    }
}
