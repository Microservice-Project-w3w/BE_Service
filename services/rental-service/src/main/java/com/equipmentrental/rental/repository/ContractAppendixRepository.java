package com.equipmentrental.rental.repository;

import com.equipmentrental.rental.entity.ContractAppendix;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractAppendixRepository extends JpaRepository<ContractAppendix, Long> {
    List<ContractAppendix> findByContractIdOrderByCreatedAtDesc(Long contractId);
}
