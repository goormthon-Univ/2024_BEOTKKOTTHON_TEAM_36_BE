package mongkey.maeilmail.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mongkey.maeilmail.common.exception.NotFoundException;
import mongkey.maeilmail.common.response.ApiResponse;
import mongkey.maeilmail.common.response.Error;
import mongkey.maeilmail.domain.Post;
import mongkey.maeilmail.domain.User;
import mongkey.maeilmail.domain.enums.CategoryType;
import mongkey.maeilmail.dto.PageInfo;
import mongkey.maeilmail.dto.like.LikePostRequestDto;
import mongkey.maeilmail.dto.post.response.PostDto;
import mongkey.maeilmail.dto.post.response.PostResponseDto;
import mongkey.maeilmail.dto.post.request.SavePostRequestDto;
import mongkey.maeilmail.dto.post.request.UpdatePostRequestDto;
import mongkey.maeilmail.dto.post.response.AllPostResponseDto;
import mongkey.maeilmail.dto.post.response.PostByCategoryDto;
import mongkey.maeilmail.repository.PostLikeRepository;
import mongkey.maeilmail.repository.PostRepository;
import mongkey.maeilmail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import mongkey.maeilmail.common.response.Success;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
//
    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final PostLikeRepository postLikeRepository;

    @Autowired
    private final UserRepository userRepository;
//
//    private Post post;
//
//
    @Transactional
    public ApiResponse<?> savePost(SavePostRequestDto requestDto){
        User user = userRepository.findById(requestDto.getUser_id())
                .orElseThrow(()-> new NotFoundException("해당 아이디를 가진 유저가 존재하지 않습니다.") {});
        Post post = postRepository.save(requestDto.toEntity(user));
        return ApiResponse.success(Success.CREATE_POST_SUCCESS, setResponseDto(post));
    }

    @Transactional
    public ApiResponse<?> findAllPost(){
        List<Post> allPost = postRepository.findAll();
        List<PostDto> postDtoList = allPost.stream()
                .map(post -> PostDto.builder()
                        .post_id(post.getId())
                        .writer(post.getUser().getName())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .created_at(post.getCreated_at())
                        .updated_at(post.getUpdated_at())
                        .build()).toList();

        return ApiResponse.success(Success.SUCCESS, AllPostResponseDto.builder().allPostList(postDtoList).build());
    }

//    /*카테고리별 게시글 조회*/
    public ApiResponse<?> findPostByCategory(CategoryType categoryType, Pageable pageable) {

        Page<Post> allPostByCategory = postRepository.findByCategory(pageable, categoryType.toString());
        System.out.println("allPostByCategory.toString() = " + allPostByCategory.toString());
        //set response
        //Set Response Dtos
        List<Post>postList = setPostList(categoryType, allPostByCategory);
        PageInfo pageInfo = setPageInfo(allPostByCategory);
        return ApiResponse.success(Success.SUCCESS, PostByCategoryDto.builder()
                .pageInfo(pageInfo)
                .categoryType(categoryType)
                .postList(postList)
                .build());
//        //리턴할 때 json 형식 맞춰서 각 페이지 별 게시글 리턴
//        return ApiResponse.success(Success.SUCCESS, "카테고리별 게시글 조회에 성공했습니다");
    }

    @Transactional
    public ApiResponse<?> updatePost(Long post_id, UpdatePostRequestDto requestDto){
        User user = userRepository.findById(requestDto.getUser_id())
                .orElseThrow(()-> new NotFoundException("해당 아이디를 가진 유저가 존재하지 않습니다.") {});

        Optional<Post> findPost = postRepository.findById(post_id);
        if (!findPost.isPresent()){
            return ApiResponse.failure(Error.NO_PERMISSION_TO_POST, "찾으려는 데이터가 없습니다");
        }
        Post post = findPost.get();

        List<UpdatePostRequestDto> list = new ArrayList<>();
        list.add(requestDto);

        for (int i = 0; i < list.size(); i++){
            if(list.get(i).getCategory() != null) post.setCategory(list.get(i).getCategory());
            if(list.get(i).getTitle() != null) post.setTitle(list.get(i).getTitle());
            if(list.get(i).getContent() != null) post.setContent(list.get(i).getContent());
        }
        Post newPost = postRepository.save(post);

        return ApiResponse.success(Success.SUCCESS, setResponseDto(newPost));
    }

    @Transactional
    public ApiResponse<?> deletePost(Long post_id, Long user_id){
        Optional<Post> findPost = postRepository.findById(post_id);
        if (!findPost.isPresent()){
            return ApiResponse.failure(Error.NO_PERMISSION_TO_POST, "찾으려는 게시글이 없습니다");
        }
        //해당 게시글을 등록한 유저가 아니라면
        if (!findPost.get().getUser().getId().equals(user_id)){
            return ApiResponse.failure(Error.NO_PERMISSION_TO_POST, "해당 게시글에 대한 권한이 없습니");
        }
        postRepository.deleteById(post_id);
        return ApiResponse.success(Success.DELETE_POST_SUCCESS, "게시글 삭제를 완료했습니다");
    }


    @Transactional
    public ApiResponse<?> likePost(Long post_id, LikePostRequestDto requestDto){
        return ApiResponse.success(Success.SUCCESS);
    }

    public ApiResponse<?> unlikePost(Long post_id, LikePostRequestDto requestDto){
        return ApiResponse.success(Success.SUCCESS);
    }

    private PostResponseDto setResponseDto(Post post) {
        return PostResponseDto
                .builder()
                .user_id(post.getUser().getId())
                .category(post.getCategory().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .written_at(post.getCreated_at())
                .build();
    }

    private List<Post> setPostList(CategoryType categoryType, Page<Post> postPage){
        return postPage.stream()
                .map(post -> Post.builder()
                        .id(post.getId())
                        .user(post.getUser())
                        .category(categoryType)
                        .title(post.getTitle())
                        .content(post.getContent())
                        .build())
                .toList();
    }

    private PageInfo setPageInfo(Page<Post> postPage){
        return PageInfo.builder()
                .last(!postPage.hasNext())
                .previous(postPage.hasPrevious())
                .nowPage(postPage.getNumber())
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .build();
    }
}
