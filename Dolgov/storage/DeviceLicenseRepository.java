package org.example.Dolgov.storage;

import org.example.Dolgov.entity.Device;
import org.example.Dolgov.entity.DeviceLicense;
import org.example.Dolgov.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {

    // Поиск записи по licenseId
    Optional<DeviceLicense> findByLicenseId(License licenseId);

    // Поиск записи по deviceId
    Optional<DeviceLicense> findByDeviceId(Device deviceId);

    // Поиск записи по deviceId и licenseId
    Optional<DeviceLicense> findByDeviceIdAndLicenseId(Device deviceId, License licenseId);

}
