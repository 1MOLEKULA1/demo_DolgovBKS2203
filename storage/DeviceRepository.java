package org.example.Dolgov.storage;

import org.example.Dolgov.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // Поиск устройства по MAC-адресу
    Device findByMacAddress(String macAddress);

    // Поиск устройства по MAC-адресу и имени
    Optional<Device> findByMacAddressAndName(String macAddress, String deviceName);

    // Проверка существования устройства по MAC-адресу
    boolean existsByMacAddress(String macAddress);

    // Проверка существования устройства по MAC-адресу и имени
    boolean existsByMacAddressAndName(String macAddress, String deviceName);
}
