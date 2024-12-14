package org.example.kurs.repository;

import org.example.kurs.model.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {

}
