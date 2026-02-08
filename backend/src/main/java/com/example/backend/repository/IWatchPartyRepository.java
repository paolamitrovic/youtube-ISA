package com.example.backend.repository;

import com.example.backend.model.WatchParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWatchPartyRepository extends JpaRepository<WatchParty, Long> {
    List<WatchParty> findAllByOrderByIdDesc();
    Optional<WatchParty> findById(Long id);
}
