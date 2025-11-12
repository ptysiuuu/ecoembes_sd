package com.ecoembes.ecoembes.repository;

import com.ecoembes.ecoembes.domain.Dumpster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DumpsterRepository extends JpaRepository<Dumpster, String> {
    List<Dumpster> findByPostalCode(String postalCode);
}

