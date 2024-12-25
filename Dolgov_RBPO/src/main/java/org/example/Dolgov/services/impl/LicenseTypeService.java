package org.example.Dolgov.services.impl;

import org.example.Dolgov.entity.LicenseType;
import org.example.Dolgov.storage.LicenseTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//TODO: 1. saveLicenseType - что будет при попытке повторно сохранить существующий тип? (Александр)
@Service
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    @Autowired
    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    // Создание или обновление типа лицензии
    public LicenseType saveLicenseType(LicenseType licenseType) {
        // Проверяем, существует ли тип лицензии с таким же названием
        Optional<LicenseType> existingLicenseType = licenseTypeRepository.findByName(licenseType.getName());
        if (existingLicenseType.isPresent()) {
            // Если тип лицензии существует, можно выбросить исключение или вернуть существующий тип
            throw new IllegalArgumentException("Тип лицензии с таким названием уже существует: " + licenseType.getName());
        }
        return licenseTypeRepository.save(licenseType);
    }


    // Получение типа лицензии по ID
    public Optional<LicenseType> getLicenseTypeById(Long id) {
        return licenseTypeRepository.findById(id);
    }

    // Получение всех типов лицензий
    public List<LicenseType> getAllLicenseTypes() {
        return licenseTypeRepository.findAll();
    }

    // Удаление типа лицензии по ID
    public void deleteLicenseType(Long id) {
        licenseTypeRepository.deleteById(id);
    }
}
