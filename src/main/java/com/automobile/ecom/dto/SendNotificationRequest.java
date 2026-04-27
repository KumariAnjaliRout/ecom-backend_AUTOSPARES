package com.automobile.ecom.dto;




import com.automobile.ecom.entity.Category;
import com.automobile.ecom.entity.NotificationChannel;
import com.automobile.ecom.entity.NotificationType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    private UUID senderId;

    private UUID receiverId;

    private String title;

    private Category category;

//    private String category;

    private NotificationType notificationType;

//    private String notificationType;

    private String message;

    private String link;

    private NotificationChannel notificationChannel;

}
