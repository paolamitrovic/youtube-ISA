package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Tag;

@Repository
public interface ITagRepository extends JpaRepository<Tag, Long>{

	Optional<Tag> findByName(String name);
}
