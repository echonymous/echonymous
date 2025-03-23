package com.echonymous.repository;

import com.echonymous.entity.Post;
import com.echonymous.entity.PostLike;
import com.echonymous.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    Optional<PostLike> findByPostAndUser_UserId(Post post, Long userId);

    // Counts the number of likes for a given post
    int countByPost(Post post);
}
