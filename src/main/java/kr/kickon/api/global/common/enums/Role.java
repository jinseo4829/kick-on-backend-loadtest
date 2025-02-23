package kr.kickon.api.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER ("ROLE_USER, ROLE_OAUTH_FIRST_JOIN, ROLE_GUEST"),
    ADMIN ("ROLE_ADMIN, ROLE_USER, ROLE_OAUTH_FIRST_JOIN, ROLE_GUEST"),
    GUEST ("ROLE_GUEST"),
    OAUTH_FIRST_JOIN ("ROLE_OAUTH_FIRST_JOIN, ROLE_GUEST");

    private final String roles;
    public static String getIncludingRoles(String role){
        return Role.valueOf(role).getRoles();
    }
    public static String addRole(Role role, String addRole){
        String priorRoles = role.getRoles();
        priorRoles += ","+addRole;
        return priorRoles;
    }
    public static String addRole(String roles, Role role){
        return roles + "," + role.getRoles();

    }
}