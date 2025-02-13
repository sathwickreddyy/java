package com.sathwick.learning.jpa.relationships.many_to_many.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "project")
@ToString(exclude = "programmers")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    @ManyToMany(mappedBy = "projects") // name of the variable in the other entity
    private Set<Programmer> programmers;
}
