package org.example.kurs.service.impl;

import org.example.kurs.model.LicenseType;
import org.example.kurs.repository.LicenseTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    @Autowired
    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    // Создание или обновление типа лицензии
    public LicenseType saveLicenseType(LicenseType licenseType) {
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
