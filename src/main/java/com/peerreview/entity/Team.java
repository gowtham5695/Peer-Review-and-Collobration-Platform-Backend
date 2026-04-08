package com.peerreview.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // NEW: max members limit set by admin (0 = unlimited)
    @Column(name = "max_members")
    private Integer maxMembers = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    public Team() {}

    public Team(String name, int maxMembers) {
        this.name       = name;
        this.maxMembers = maxMembers;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getMaxMembers() { return maxMembers; }
    
    public Set<User> getMembers() { return members; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public void setMembers(Set<User> members) { this.members = members; }
}
