package com.resik.api.repository;

import com.resik.api.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserIdOrderByCreatedAtAsc(Long userId);

    /**
     * Returns the latest chat message for each user, so the admin can see
     * a list of all active conversations in one request.
     */
    @Query(value = """
        SELECT DISTINCT ON (c.user_id) c.*
        FROM chat c
        ORDER BY c.user_id, c.created_at DESC
        """, nativeQuery = true)
    List<Chat> findLatestPerUser();

    /**
     * Returns all userIds that have unread messages from USER (client).
     */
    @Query(value = """
        SELECT DISTINCT c.user_id FROM chat c
        WHERE c.pengirim = 'USER' AND (c.is_read IS NULL OR c.is_read = false)
        """, nativeQuery = true)
    List<Long> findUserIdsWithUnread();
}

