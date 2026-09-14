package com.example.promptdb.support;

import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.api.dto.Role;
import java.util.UUID;

/**
 * UI-level convenience flags controlling which buttons are rendered for a given group screen.
 * These are a usability shortcut only - the API is the real authorization enforcement point, and
 * every action still goes through it. Computed from the current member's global role plus their
 * membership role (if any) within the specific group being viewed.
 *
 * Assumption (undocumented in the OpenAPI spec): ADMIN can manage every group; GROUP_LEAD can
 * manage groups where their membership role for that group is also GROUP_LEAD; ordinary MEMBERs
 * can view groups/prompts they belong to and manage only their own prompts.
 */
public record GroupPermissions(
        boolean canEditGroup,
        boolean canDeleteGroup,
        boolean canManageMembers,
        boolean canCreatePrompt
) {

    public static GroupPermissions of(MemberResponse currentMember, Role membershipRoleInGroup) {
        boolean admin = currentMember != null && currentMember.role() == Role.ADMIN;
        boolean groupLead = membershipRoleInGroup == Role.GROUP_LEAD;
        boolean isMember = membershipRoleInGroup != null || admin;

        return new GroupPermissions(
                admin || groupLead,
                admin,
                admin || groupLead,
                isMember
        );
    }

    public static boolean canEditPrompt(MemberResponse currentMember, Role membershipRoleInGroup, UUID promptOwnerId) {
        boolean admin = currentMember != null && currentMember.role() == Role.ADMIN;
        boolean groupLead = membershipRoleInGroup == Role.GROUP_LEAD;
        boolean owner = currentMember != null
                && promptOwnerId != null
                && promptOwnerId.equals(currentMember.memberId());
        return admin || groupLead || owner;
    }
}
