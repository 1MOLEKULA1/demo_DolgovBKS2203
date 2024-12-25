package org.example.Dolgov.entity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Base64;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.Date;

//TODO: 1. createTicket - Нет реализации цифровой подписи. UUID - не цифровая подпись (Александр)
//TODO: 2. Тикет содержит неверную информацию о лицензии (см. Задание 4) (Александр)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tickets")  // Название таблицы в БД
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Идентификатор тикета
    private Long id;

    @Column(name = "server_date")
    private LocalDateTime serverDate;

    @Column(name = "ticket_lifetime")
    private int ticketLifetime;

    @Column(name = "activation_date")
    private Date activationDate;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "digital_signature")
    private String digitalSignature;


    //TODO: реализовать цифровую подпись

    // Метод для создания тикета
    private static String generateSignature(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(hmacData), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Ошибка при генерации подписи", e);
        }
    }


    // Метод для создания тикета
    public static Ticket createTicket(Long userId, boolean isBlocked, Date expirationDate, Long deviceId, String secretKey) {
        Ticket ticket = new Ticket();
        ticket.setServerDate(LocalDateTime.now());
        ticket.setTicketLifetime(5);
        ticket.setActivationDate(new Date());
        ticket.setExpirationDate(expirationDate);
        ticket.setUserId(userId);
        ticket.setDeviceId(deviceId);
        ticket.setBlocked(isBlocked);

        // Создание данных для подписи
        String signatureData = userId + ":" + isBlocked + ":" + expirationDate + ":" + deviceId;
        ticket.setDigitalSignature(generateSignature(signatureData, secretKey));

        return ticket;
    }
}