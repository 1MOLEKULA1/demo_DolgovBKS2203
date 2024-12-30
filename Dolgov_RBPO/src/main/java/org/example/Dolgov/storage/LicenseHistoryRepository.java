package org.example.Dolgov.storage;

import org.example.Dolgov.entity.ApplicationUser;
import org.example.Dolgov.entity.License;
import org.example.Dolgov.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {

    List<LicenseHistory> findByUserId(ApplicationUser userId);

    List<LicenseHistory> findByLicenseId(License licenseId);
}