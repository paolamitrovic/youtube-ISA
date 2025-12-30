package com.example.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.model.Video;


@Repository
public interface IVideoRepository extends JpaRepository<Video, Long> {
    
	List<Video> findAllByOrderByCreatedAtDesc();

}
