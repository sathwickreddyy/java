package com.sathwick.learning.jpa.relationships.many_to_many.repo;

import com.sathwick.learning.jpa.relationships.many_to_many.domain.Programmer;
import org.springframework.data.repository.CrudRepository;

public interface ProgrammerRepository extends CrudRepository<Programmer, Integer> {
}
