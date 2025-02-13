package com.sathwick.learning.jpa.relationships.many_to_many.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Setter
@Getter
@Entity
@ToString(exclude = "projects")
@Table(name = "programmer")
public class Programmer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int salary;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "programmers_projects",
            joinColumns = @JoinColumn(name = "programmer_id", referencedColumnName = "id"), // foreign key (programmer_id) references programmer(id),
            inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id")) // foreign key (project_id) references project(id)
    private Set<Project> projects;
}
