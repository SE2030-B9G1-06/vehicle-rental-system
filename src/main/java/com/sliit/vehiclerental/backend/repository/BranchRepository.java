package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.Branch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {
  Optional<Branch> findByNameIgnoreCase(String name);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select b from Branch b where b.id = :id")
  Optional<Branch> lockById(@org.springframework.data.repository.query.Param("id") Integer id);
}
