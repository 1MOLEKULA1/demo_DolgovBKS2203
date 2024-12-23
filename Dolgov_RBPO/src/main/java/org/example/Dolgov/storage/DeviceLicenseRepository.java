package org.example.Dolgov.storage;

import org.example.Dolgov.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {

    // Поиск записи по licenseId
    Optional<DeviceLicense> findByLicenseId(Long licenseId);

    // Поиск записи по deviceId
    Optional<DeviceLicense> findByDeviceId(Long deviceId);

    // Поиск записи по deviceId и licenseId
    Optional<DeviceLicense> findByDeviceIdAndLicenseId(Long deviceId, Long licenseId);

    // Проверка, существует ли запись с указанным licenseId
    boolean existsByLicenseId(Long licenseId);

    // Проверка, существует ли запись с указанным deviceId
    boolean existsByDeviceId(Long deviceId);

    // Проверка, существует ли запись с заданным deviceId и licenseId
    boolean existsByDeviceIdAndLicenseId(Long deviceId, Long licenseId);
}
