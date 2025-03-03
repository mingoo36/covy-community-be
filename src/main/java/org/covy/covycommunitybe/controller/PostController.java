package org.covy.covycommunitybe.controller;

import org.covy.covycommunitybe.model.Post;
import org.covy.covycommunitybe.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 모든 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        List<Post> posts = postService.getAllPosts();

        List<Map<String, Object>> postList = posts.stream().map(post -> {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("post_id", post.getPostId());
            postMap.put("title", post.getTitle());
            postMap.put("content", post.getContent());
            postMap.put("views", post.getViews());
            postMap.put("created_at", post.getCreatedAt());
            postMap.put("updated_at", post.getUpdatedAt());
            postMap.put("author", post.getUser().getUsername());
            postMap.put("author_image", Optional.ofNullable(post.getUser().getImage())
                    .orElse("http://localhost:8080/default_images/default_profile.webp"));

            return postMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(postList);
    }


}

