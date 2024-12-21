package org.example.Dolgov.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.LicenseHistory;
import org.example.Dolgov.storage.LicenseHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LicenseHistoryService {

    private final LicenseHistoryRepository licenseHistoryRepository;

    // Метод для записи изменений в истории лицензий
    public void recordLicenseChange(Long licenseId, Long userId, String status, Date changeDate, String description) {
        // Создаем объект истории лицензии
        LicenseHistory history = new LicenseHistory();
        history.setLicenseId(licenseId); // Устанавливаем идентификатор лицензии
        history.setUserId(userId); // Устанавливаем идентификатор пользователя, который сделал изменение
        history.setStatus(status); // Устанавливаем статус изменения лицензии (например, активирована, заблокирована и т. д.)
        history.setChangeDate(changeDate); // Устанавливаем дату изменения
        history.setDescription(description); // Устанавливаем описание изменения

        // Сохраняем запись в базе данных через репозиторий
        licenseHistoryRepository.save(history);
    }
}