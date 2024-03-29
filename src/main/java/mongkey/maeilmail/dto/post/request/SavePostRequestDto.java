package mongkey.maeilmail.dto.post.request;

import lombok.Getter;
import mongkey.maeilmail.domain.Post;
import mongkey.maeilmail.domain.User;
import mongkey.maeilmail.domain.enums.CategoryType;

@Getter
public class SavePostRequestDto {
    private Long user_id;
    private CategoryType category;
    private String title;
    private String content;

//        private PostType postType;

    //SavePostRequestDto를 실제 엔티티로 변환
    public Post toEntity(User user){
        return Post.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .build();
    }

    @Override
    public String toString() {
        return "SavePostRequestDto{" +
                "user_id='" + user_id + '\'' +
                ", category=" + category +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
