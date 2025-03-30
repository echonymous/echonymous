package com.echonymous.repository;

import com.echonymous.entity.Post;
import com.echonymous.entity.PostEcho;
import com.echonymous.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostEchoRepository extends JpaRepository<PostEcho, Long> {
    Optional<PostEcho> findByPostAndUser(Post post, User user);
    Optional<PostEcho> findByPostAndUser_UserId(Post post, Long userId);

    int countByPost(Post post);
}
