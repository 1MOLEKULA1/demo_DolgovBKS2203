package org.example.Dolgov.services.impl;

import org.example.Dolgov.entity.Device;
import org.example.Dolgov.entity.DeviceLicense;
import org.example.Dolgov.entity.License;
import org.example.Dolgov.storage.DeviceLicenseRepository;
import org.example.Dolgov.storage.DeviceRepository;
import org.example.Dolgov.storage.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseRepository licenseRepository;

    // Внедрение зависимостей через конструктор
    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         DeviceLicenseRepository deviceLicenseRepository,
                         LicenseRepository licenseRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseRepository = licenseRepository;
    }

    // Метод для сохранения или обновления устройства
    public Device saveDevice(Device device) {
        return deviceRepository.save(device);  // Сохраняем устройство в базе данных
    }

    // Метод для получения устройства по его ID
    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);  // Возвращаем Optional для безопасной работы с отсутствующими данными
    }

    // Метод для получения устройства по MAC-адресу
    public Device getDeviceByMacAddress(String macAddress) {
        return deviceRepository.findByMacAddress(macAddress);  // Ищем устройство по MAC-адресу
    }

    // Метод для получения списка всех устройств
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();  // Возвращаем все устройства из базы данных
    }

    // Метод для удаления устройства по ID с дополнительной логикой
    public void deleteDevice(Long id) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);  // Получаем устройство по ID

        if (deviceOpt.isPresent()) {  // Если устройство найдено
            Device device = deviceOpt.get();

            // 1. Удаляем запись о привязке устройства к лицензии из таблицы DeviceLicense
            Optional<DeviceLicense> deviceLicenseOpt = deviceLicenseRepository.findByDeviceId(id);
            if (deviceLicenseOpt.isPresent()) {
                DeviceLicense deviceLicense = deviceLicenseOpt.get();

                Optional<License> licenseOpt = licenseRepository.findById(deviceLicense.getLicenseId());

                // 2. Получаем лицензию, которая привязана к устройству
                License license = licenseOpt.get();

                // 3. Увеличиваем количество доступных устройств в лицензии
                license.setDeviceCount(license.getDeviceCount() + 1);  // Увеличиваем количество устройств
                licenseRepository.save(license);  // Сохраняем обновленную лицензию

                // Удаляем запись о привязке устройства к лицензии
                deviceLicenseRepository.delete(deviceLicense);
            }

            // 4. Удаляем само устройство из базы данных
            deviceRepository.delete(device);
        }
    }
}
