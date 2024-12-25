package org.example.Dolgov.storage;

import org.example.Dolgov.entity.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
    Optional<LicenseType> findByName(String name);

}