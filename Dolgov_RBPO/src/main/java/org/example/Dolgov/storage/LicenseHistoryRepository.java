package org.example.Dolgov.storage;

import org.example.Dolgov.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
    // Дополнительные методы, если нужно
}
