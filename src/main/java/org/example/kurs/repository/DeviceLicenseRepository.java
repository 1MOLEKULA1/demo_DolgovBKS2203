package org.example.kurs.repository;

import org.example.kurs.model.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {

    // Метод для поиска по license_id
    Optional<DeviceLicense> findByLicenseId(Long licenseId);

    Optional<DeviceLicense> findByDeviceId(Long deviceId);

    // Метод для поиска записи по deviceId и licenseId
    Optional<DeviceLicense> findByDeviceIdAndLicenseId(Long deviceId, Long licenseId);

}
