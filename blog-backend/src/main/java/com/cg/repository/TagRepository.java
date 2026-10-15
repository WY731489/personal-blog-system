package com.cg.repository;

import com.cg.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByNameIn(Set<String> names);

    List<Tag> findByIdIn(Set<Long> ids);
}
