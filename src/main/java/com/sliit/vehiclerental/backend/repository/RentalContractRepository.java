package com.sliit.vehiclerental.backend.repository;

import com.sliit.vehiclerental.backend.entity.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
}