package com.example.promptdb.web;

import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.api.GroupApiClient;
import com.example.promptdb.api.GroupMembershipApiClient;
import com.example.promptdb.api.MemberApiClient;
import com.example.promptdb.api.dto.AddMemberRequest;
import com.example.promptdb.api.dto.ChangeMemberRoleRequest;
import com.example.promptdb.api.dto.GroupMembershipResponse;
import com.example.promptdb.api.dto.Role;
import com.example.promptdb.service.GroupContextService;
import com.example.promptdb.support.GroupPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class GroupMemberControllerTest extends ControllerTestSupport {

    private static final GroupPermissions PERMISSIONS = new GroupPermissions(true, true, true, true);
    private static final GroupMembershipResponse MEMBERSHIP = new GroupMembershipResponse(GROUP_ID, MEMBER_ID, Role.GROUP_LEAD);

    @MockitoBean
    private GroupMembershipApiClient groupMembershipApiClient;

    @MockitoBean
    private GroupApiClient groupApiClient;

    @MockitoBean
    private MemberApiClient memberApiClient;

    @MockitoBean
    private GroupContextService groupContextService;

    @DisplayName("List renders group members and permissions")
    @Test
    void listRendersGroupMembersAndPermissions() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(groupMembershipApiClient.listGroupMembers(GROUP_ID)).willReturn(List.of(MEMBERSHIP));
        given(groupContextService.permissions(GROUP_ID)).willReturn(PERMISSIONS);

        mockMvc.perform(get("/groups/{groupId}/members", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/list"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("memberships", List.of(MEMBERSHIP)))
                .andExpect(model().attribute("permissions", PERMISSIONS));
    }

    @DisplayName("Add form renders candidate members")
    @Test
    void addFormRendersCandidateMembers() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(memberApiClient.listMembers()).willReturn(List.of(ALICE, BOB));

        mockMvc.perform(get("/groups/{groupId}/members/new", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/form"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("candidateMembers", List.of(ALICE, BOB)))
                .andExpect(model().attributeExists("groupMemberForm"));
    }

    @DisplayName("Add post with validation errors returns form")
    @Test
    void addPostWithValidationErrorsReturnsForm() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(memberApiClient.listMembers()).willReturn(List.of(ALICE, BOB));

        mockMvc.perform(post("/groups/{groupId}/members", GROUP_ID).with(csrf())
                        .param("memberId", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/form"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("candidateMembers", List.of(ALICE, BOB)));
    }

    @DisplayName("Add post adds member and redirects to list")
    @Test
    void addPostAddsMemberAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/members", GROUP_ID).with(csrf())
                        .param("memberId", MEMBER_ID.toString())
                        .param("role", "MEMBER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/members"));

        verify(groupMembershipApiClient).addMember(GROUP_ID, new AddMemberRequest(MEMBER_ID, Role.MEMBER));
    }

    @DisplayName("Add post when API validation fails shows error message")
    @Test
    void addPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(memberApiClient.listMembers()).willReturn(List.of(ALICE, BOB));
        given(groupMembershipApiClient.addMember(any(), any())).willThrow(new ApiValidationException("duplicate", 422));

        mockMvc.perform(post("/groups/{groupId}/members", GROUP_ID).with(csrf())
                        .param("memberId", MEMBER_ID.toString())
                        .param("role", "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this membership: duplicate"));
    }

    @DisplayName("Edit form renders existing membership role")
    @Test
    void editFormRendersExistingMembershipRole() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(groupMembershipApiClient.listGroupMembers(GROUP_ID)).willReturn(List.of(MEMBERSHIP));

        mockMvc.perform(get("/groups/{groupId}/members/{memberId}/edit", GROUP_ID, MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/edit-role"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("memberId", MEMBER_ID))
                .andExpect(model().attributeExists("changeRoleForm"));
    }

    @DisplayName("Update role post with validation errors returns form")
    @Test
    void updateRolePostWithValidationErrorsReturnsForm() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(post("/groups/{groupId}/members/{memberId}", GROUP_ID, MEMBER_ID).with(csrf())
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/edit-role"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("memberId", MEMBER_ID));
    }

    @DisplayName("Update role post changes role and redirects to list")
    @Test
    void updateRolePostChangesRoleAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/members/{memberId}", GROUP_ID, MEMBER_ID).with(csrf())
                        .param("role", "GROUP_LEAD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/members"));

        verify(groupMembershipApiClient).changeMemberRole(
                GROUP_ID, MEMBER_ID, new ChangeMemberRoleRequest(Role.GROUP_LEAD)
        );
    }

    @DisplayName("Update role post when API validation fails shows error message")
    @Test
    void updateRolePostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(groupMembershipApiClient.changeMemberRole(any(), any(), any()))
                .willThrow(new ApiValidationException("bad", 422));

        mockMvc.perform(post("/groups/{groupId}/members/{memberId}", GROUP_ID, MEMBER_ID).with(csrf())
                        .param("role", "GROUP_LEAD"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members/edit-role"))
                .andExpect(model().attribute("errorMessage", "The API rejected this role change: bad"));
    }

    @DisplayName("Remove post removes member and redirects to list")
    @Test
    void removePostRemovesMemberAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/members/{memberId}/delete", GROUP_ID, MEMBER_ID).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/members"));

        verify(groupMembershipApiClient).removeMember(GROUP_ID, MEMBER_ID);
    }
}
