/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.accessdenied.api;

import com.arpanrec.accessdenied.AccessDeniedConstants;
import com.arpanrec.accessdenied.auth.AuthenticationImpl;
import com.arpanrec.accessdenied.exceptions.UserNotFoundException;
import com.arpanrec.accessdenied.models.Role;
import com.arpanrec.accessdenied.models.User;
import com.arpanrec.accessdenied.services.RoleServices;
import com.arpanrec.accessdenied.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = AccessDeniedConstants.API_ENDPOINT + "/users")
@Tag(name = "User Management", description = "Operations related to managing users in the system.")
public class UsersAPI {

    private final UserService userService;
    private final RoleServices roleServices;

    public UsersAPI(@Autowired UserService userService, @Autowired RoleServices roleServices) {
        this.userService = userService;
        this.roleServices = roleServices;
    }

    @Operation(security = {@SecurityRequirement(name = AccessDeniedConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(
            path = "/user/{username}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<User> getUser(@PathVariable String username) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        User user = userService
                .findByUsernameAndNamespace(username, auth.getNamespace())
                .orElseThrow(() -> new UserNotFoundException("User not " + "found with username: " + username
                        + " and namespace: " + auth.getNamespace().getName()));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping(
            path = "/user/{username}",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = AccessDeniedConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    public HttpEntity<User> createUpdateUser(
            @PathVariable String username, @RequestBody UpdateUserRequest userDetails) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.debug("Updating user {}", username);
        User user = userService
                .findByUsernameAndNamespace(username, auth.getNamespace())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    return newUser;
                });

        user.setUsername(username);

        if (userDetails.email != null && !userDetails.email.isBlank()) {
            user.setEmail(userDetails.email);
        }

        if (userDetails.roles != null) {
            user.getRoles().clear();
            for (String roleName : userDetails.roles) {
                Role role = roleServices.getRole(roleName, auth.getNamespace());
                user.getRoles().add(role);
            }
        }
        user.setLocked(userDetails.locked);
        user.setEnabled(userDetails.enabled);
        log.debug("Updated user {}", user);

        if (userDetails.password != null) {
            log.debug("Updating password for user {}", username);
            user.setPassword(userDetails.password);
        }

        user = userService.saveUser(user, auth.getNamespace());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    public record UpdateUserRequest(String email, String password, String[] roles, boolean locked, boolean enabled) {}
}
