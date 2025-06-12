package com.cofeecode.application.powerhauscore.dto;

// Assuming Role enum/class is in this package, adjust if necessary
import com.cofeecode.application.powerhauscore.data.Role;
import java.util.Set;

public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String name;
    private Set<Role> roles;

    public JwtResponseDTO(String token, Long id, String username, String name, Set<Role> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.roles = roles;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
