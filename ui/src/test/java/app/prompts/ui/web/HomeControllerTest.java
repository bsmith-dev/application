package app.prompts.ui.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeControllerTest extends ControllerTestSupport {

    @DisplayName("Home redirects to login when not authenticated")
    @Test
    void homeRedirectsToLoginWhenNotAuthenticated() throws Exception {
        given(currentApiSession.isAuthenticated()).willReturn(false);
        given(currentApiSession.currentMember()).willReturn(Optional.empty());
        given(currentApiSession.accessToken()).willReturn(Optional.empty());

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @DisplayName("Home redirects to dashboard when authenticated")
    @Test
    void homeRedirectsToDashboardWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }
}
