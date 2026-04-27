package com.automobile.ecom.dto;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {

    private UUID userId;

    private Long unreadCount;

}