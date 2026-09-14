package app.prompts.ui.web;

import app.prompts.ui.api.dto.GroupResponse;
import app.prompts.ui.api.dto.MemberResponse;
import app.prompts.ui.api.dto.OrganizationResponse;
import app.prompts.ui.api.dto.Role;
import app.prompts.ui.security.CurrentApiSession;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@TestPropertySource(properties = {"app.api.base-url=http://127.0.0.1:1"})
abstract class ControllerTestSupport {

    protected static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    protected static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    protected static final UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
    protected static final UUID OTHER_MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
    protected static final UUID PROMPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");

    protected static final MemberResponse ALICE = new MemberResponse(
            MEMBER_ID, ORGANIZATION_ID, "alice", "alice@example.com", Role.ADMIN
    );
    protected static final MemberResponse BOB = new MemberResponse(
            OTHER_MEMBER_ID, ORGANIZATION_ID, "bob", "bob@example.com", Role.MEMBER
    );
    protected static final OrganizationResponse ORGANIZATION = new OrganizationResponse(ORGANIZATION_ID, "Acme");
    protected static final GroupResponse GROUP = new GroupResponse(GROUP_ID, ORGANIZATION_ID, "Engineering");

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @MockitoBean
    protected CurrentApiSession currentApiSession;

    @BeforeEach
    void setUpControllerTestSupport() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        given(currentApiSession.isAuthenticated()).willReturn(true);
        given(currentApiSession.currentMember()).willReturn(Optional.of(ALICE));
        given(currentApiSession.accessToken()).willReturn(Optional.of("token"));
    }
}
