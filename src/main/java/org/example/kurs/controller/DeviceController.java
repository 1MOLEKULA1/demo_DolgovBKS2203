package org.example.kurs.controller;

import org.example.kurs.model.Device;
import org.example.kurs.service.impl.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления устройствами.
 * Обрабатывает запросы для создания, обновления, получения и удаления устройств.
 */
@RestController
@RequestMapping("/api/devices") // Базовый маршрут для управления устройствами
public class DeviceController {

    private final DeviceService deviceService; // Сервис для управления устройствами

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService; // Внедрение зависимости сервиса
    }

    /**
     * Создание или обновление устройства.
     *
     * @param device Объект устройства, который нужно создать или обновить.
     * @return ResponseEntity с сохраненным устройством и статусом 201 Created.
     */
    @PostMapping
    public ResponseEntity<Device> createOrUpdateDevice(@RequestBody Device device) {
        Device savedDevice = deviceService.saveDevice(device); // Сохраняем устройство через сервис
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDevice); // Возвращаем сохраненное устройство
    }

    /**
     * Получение устройства по идентификатору (ID).
     *
     * @param id Идентификатор устройства.
     * @return ResponseEntity с устройством, если найдено, или статусом 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        Optional<Device> device = deviceService.getDeviceById(id); // Ищем устройство по ID
        return device.map(ResponseEntity::ok) // Если устройство найдено, возвращаем его
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Иначе 404
    }

    /**
     * Получение устройства по MAC-адресу.
     *
     * @param macAddress MAC-адрес устройства.
     * @return ResponseEntity с устройством, если найдено, или статусом 404 Not Found.
     */
    @GetMapping("/mac/{macAddress}")
    public ResponseEntity<Device> getDeviceByMacAddress(@PathVariable String macAddress) {
        Device device = deviceService.getDeviceByMacAddress(macAddress); // Ищем устройство по MAC-адресу
        return device != null ? ResponseEntity.ok(device) // Если устройство найдено, возвращаем его
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Иначе 404
    }

    /**
     * Получение списка всех устройств.
     *
     * @return ResponseEntity со списком всех устройств.
     */
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices(); // Получаем список всех устройств
        return ResponseEntity.ok(devices); // Возвращаем список устройств
    }

    /**
     * Удаление устройства по идентификатору (ID).
     *
     * @param id Идентификатор устройства.
     * @return ResponseEntity со статусом 204 No Content, если удалено, или 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        Optional<Device> device = deviceService.getDeviceById(id); // Проверяем, существует ли устройство
        if (device.isPresent()) {
            deviceService.deleteDevice(id); // Удаляем устройство
            return ResponseEntity.noContent().build(); // Возвращаем статус 204 (успех без содержимого)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Возвращаем статус 404, если устройство не найдено
        }
    }
}