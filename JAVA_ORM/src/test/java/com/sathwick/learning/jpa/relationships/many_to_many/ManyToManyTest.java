package com.sathwick.learning.jpa.relationships.many_to_many;

import com.sathwick.learning.jpa.relationships.many_to_many.domain.Programmer;
import com.sathwick.learning.jpa.relationships.many_to_many.domain.Project;
import com.sathwick.learning.jpa.relationships.many_to_many.repo.ProgrammerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManyToManyTest {
    @Autowired
    ProgrammerRepository programmerRepository;
    @Test
    public void testCreateProgrammer() {
        Programmer programmer = new Programmer();
        programmer.setName("Bunny Reddy");
        programmer.setSalary(100000);

        HashSet<Project> projects = new HashSet<>();
        Project p1 = new Project();
        p1.setName("Kafka project");
        projects.add(p1);
        Project p2 = new Project();
        p2.setName("Spring project");
        projects.add(p2);

        programmer.setProjects(projects);
        programmerRepository.save(programmer);
    }

    @Test
    public void testLoadProgrammer() {
        Optional<Programmer> optionalProgrammer = programmerRepository.findById(52);
        assertTrue(optionalProgrammer.isPresent());

        Programmer programmer = optionalProgrammer.get();
        System.out.println(programmer);

        System.out.println(programmer.getProjects());
    }

}