package mongkey.maeilmail.dto.post.response;

import lombok.Builder;
import lombok.Getter;
import mongkey.maeilmail.domain.enums.CategoryType;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {
    private Long user_id;
    private String category;
    private String title;
    private String content;
//    private String user_id;
    private LocalDateTime written_at;
}
