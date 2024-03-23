package mongkey.maeilmail.controller;

import lombok.RequiredArgsConstructor;
import mongkey.maeilmail.common.response.ApiResponse;
import mongkey.maeilmail.domain.enums.CategoryType;
import mongkey.maeilmail.dto.like.LikePostRequestDto;
import mongkey.maeilmail.dto.post.request.SavePostRequestDto;
import mongkey.maeilmail.dto.post.request.UpdatePostRequestDto;
import mongkey.maeilmail.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {
    @Autowired
    private final PostService postService;

    @GetMapping("/post")
    public ApiResponse<?> findAllPost() {
        return postService.findAllPost();
    }

    /*특정 카테고리 게시글 조회*/
    @GetMapping("/post/category")
    public ApiResponse<?> findPostByCategory(@RequestParam(name = "category_type") CategoryType categoryType,
                                             @PageableDefault(size = 6) Pageable pageable){
        return postService.findPostByCategory(categoryType, pageable);
    }

    /*게시글 등록*/
    @PostMapping("/post")
    public ApiResponse<?> savePost(@RequestBody SavePostRequestDto requestDto){
        String string = requestDto.toString();
        return postService.savePost(requestDto);
    }

    /*게시글 조회*/
    @PatchMapping("/post/{post_id}")
    public ApiResponse<?> updatePost(@PathVariable Long post_id,
                                     @RequestBody UpdatePostRequestDto requestDto){
        return postService.updatePost(post_id, requestDto);
    }

    /*게시글 삭제*/
    @DeleteMapping("/post/{post_id}")
    public ApiResponse<?> deletePost(@PathVariable Long post_id,
                                     @RequestParam("user_id") Long user_id){
        return postService.deletePost(post_id, user_id);
    }

    @GetMapping("/post/{post_id}/like")
    public ApiResponse<?> likePost(@PathVariable Long post_id,
                                   @RequestBody LikePostRequestDto requestDto) {

        return postService.likePost(post_id, requestDto);
    }

//    @GetMapping("/post/{post_id}/unlike")
//    public ApiResponse<?> unlikePost(@PathVariable Long post_id,
//                                     @RequestBody LikePostRequestDto requestDto) {
//
//        return postService.unlikePost(post_id);
//    }


}