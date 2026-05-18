package com.commerce.FarmerDirectMarkert.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDto {

    private String approvalStatus;
    private List<ChatMessageResponse> messages;
    
}
