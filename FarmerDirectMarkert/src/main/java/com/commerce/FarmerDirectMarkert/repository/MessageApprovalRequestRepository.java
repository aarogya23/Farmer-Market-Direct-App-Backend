package com.commerce.FarmerDirectMarkert.repository;

import com.commerce.FarmerDirectMarkert.model.MessageApprovalRequest;
import com.commerce.FarmerDirectMarkert.model.MessageApprovalRequest.ApprovalStatus;
import com.commerce.FarmerDirectMarkert.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageApprovalRequestRepository extends JpaRepository<MessageApprovalRequest, String> {

    List<MessageApprovalRequest> findByRecipientAndStatus(User recipient, ApprovalStatus status);

    List<MessageApprovalRequest> findBySenderAndStatus(User sender, ApprovalStatus status);

    List<MessageApprovalRequest> findByRecipient(User recipient);

    List<MessageApprovalRequest> findBySender(User sender);
}
