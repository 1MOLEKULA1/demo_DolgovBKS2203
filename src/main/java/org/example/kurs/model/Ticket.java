package org.example.kurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.Date;

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

    // Метод для создания тикета
    public static Ticket createTicket(Long userId, boolean isBlocked, Date expirationDate) {
        Ticket ticket = new Ticket();
        ticket.setServerDate(LocalDateTime.now());  // Устанавливаем текущую дату и время
        ticket.setTicketLifetime(5);  // Срок жизни тикета, например, 5 дней
        ticket.setActivationDate(new Date());  // Устанавливаем текущую дату как дату активации тикета
        ticket.setExpirationDate(expirationDate);   // Устанавливаем дату истечения (если передана)
        ticket.setUserId(userId);                    // Устанавливаем ID пользователя, для которого создается тикет
                                        // Не указываем устройство (если нужно, можно передать)
        ticket.setBlocked(isBlocked);              // Устанавливаем, заблокирован ли тикет
        ticket.setDigitalSignature(UUID.randomUUID().toString());  // Генерация уникальной цифровой подписи для тикета

        return ticket;  // Возвращаем созданный тикет
    }


}
