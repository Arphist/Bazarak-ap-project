package com.bazarak.repository;

import com.bazarak.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find a conversation by buyer, seller, and advertisement.
     * Used to check for duplicate conversations.
     *
     * @param buyerId the ID of the buyer
     * @param sellerId the ID of the seller
     * @param advertisementId the ID of the advertisement
     * @return Optional containing the conversation if found
     */
    Optional<Conversation> findByBuyerIdAndSellerIdAndAdvertisementId(
            Long buyerId, Long sellerId, Long advertisementId);

    /**
     * Check if a conversation already exists between buyer and seller for an ad.
     *
     * @param buyerId the ID of the buyer
     * @param sellerId the ID of the seller
     * @param advertisementId the ID of the advertisement
     * @return true if conversation exists, false otherwise
     */
    boolean existsByBuyerIdAndSellerIdAndAdvertisementId(
            Long buyerId, Long sellerId, Long advertisementId);

    /**
     * Get all conversations where the user is a participant (either as buyer or seller).
     * Results are ordered by updatedAt descending (most recent first).
     *
     * @param userId the ID of the user
     * @return list of conversations
     */
    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.buyer " +
            "LEFT JOIN FETCH c.seller " +
            "LEFT JOIN FETCH c.advertisement " +
            "WHERE c.buyer.id = :userId OR c.seller.id = :userId " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByParticipantId(@Param("userId") Long userId);

    /**
     * Get all conversations for a specific advertisement.
     *
     * @param advertisementId the ID of the advertisement
     * @return list of conversations
     */
    List<Conversation> findByAdvertisementId(Long advertisementId);

    /**
     * Get all conversations where the user is the seller.
     *
     * @param sellerId the ID of the seller
     * @return list of conversations
     */
    List<Conversation> findBySellerId(Long sellerId);

    /**
     * Get all conversations where the user is the buyer.
     *
     * @param buyerId the ID of the buyer
     * @return list of conversations
     */
    List<Conversation> findByBuyerId(Long buyerId);

    /**
     * Get conversation with messages loaded eagerly.
     * Used when we need to display messages along with conversation details.
     *
     * @param conversationId the ID of the conversation
     * @return Optional containing the conversation with messages
     */
    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.messages " +
            "WHERE c.id = :conversationId")
    Optional<Conversation> findByIdWithMessages(@Param("conversationId") Long conversationId);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.messages " +
            "LEFT JOIN FETCH c.buyer " +
            "LEFT JOIN FETCH c.seller " +
            "LEFT JOIN FETCH c.advertisement " +
            "WHERE c.id = :conversationId")
    Optional<Conversation> findConversationWithDetails(@Param("conId") Long conId);
}