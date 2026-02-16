package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.backend.model.Video;

import jakarta.transaction.Transactional;


@Repository
public interface IVideoRepository extends JpaRepository<Video, Long> {
    
	List<Video> findAllByOrderByCreatedAtDesc();
	
	@Modifying
	@Transactional
	@Query("UPDATE Video v SET v.views = v.views + 1 WHERE v.id = :id")
	void incrementViews(Long id);

}
