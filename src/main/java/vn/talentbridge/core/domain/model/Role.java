package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.RoleName;

public class Role {
    private Long id;
    private RoleName name;
    private String description;

    public Role() {}

    public Role(Long id, RoleName name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoleName getName() { return name; }
    public void setName(RoleName name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}