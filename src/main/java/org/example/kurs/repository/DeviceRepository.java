package org.example.kurs.repository;

import org.example.kurs.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // Метод для поиска устройства по MAC-адресу, имени и ID пользователя
    Device findByMacAddress(String macAddress);
    // Поиск устройства по MAC-адресу и имени
    Optional<Device> findByMacAddressAndName(String macAddress, String deviceName);
}

