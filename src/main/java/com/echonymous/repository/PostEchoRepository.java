package com.echonymous.repository;

import com.echonymous.entity.Post;
import com.echonymous.entity.PostEcho;
import com.echonymous.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostEchoRepository extends JpaRepository<PostEcho, Long> {
    Optional<PostEcho> findByPostAndUser(Post post, User user);
    Optional<PostEcho> findByPostAndUser_UserId(Post post, Long userId);

    int countByPost(Post post);

    @Query("SELECT pe FROM PostEcho pe WHERE pe.user.userId = :userId AND TYPE(pe.post) = TextPost ORDER BY " +
            "pe.echoedAt DESC")
    List<PostEcho> findTextPostEchoedByUser(@Param("userId") Long userId, Pageable pageable);

    // With pagination using a cursor (fetch echoed with echoedAt before the given timestamp)
    @Query("SELECT pe FROM PostEcho pe WHERE pe.user.userId = :userId AND TYPE(pe.post) = TextPost AND " +
            "pe.echoedAt < :cursor ORDER BY pe.echoedAt DESC")
    List<PostEcho> findTextPostEchoedByUserBefore(@Param("userId") Long userId,
                                                  @Param("cursor") LocalDateTime cursor,
                                                  Pageable pageable);
}
