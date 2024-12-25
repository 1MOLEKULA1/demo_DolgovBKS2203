package org.example.Dolgov.storage;

import org.example.Dolgov.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {

    List<LicenseHistory> findByUserId(Long userId);

    List<LicenseHistory> findByLicenseId(Long licenseId);
}