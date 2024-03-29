package mongkey.maeilmail.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mongkey.maeilmail.common.domain.BaseTimeEntity;
import mongkey.maeilmail.domain.enums.CategoryType;
import org.w3c.dom.Text;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "post")
@Entity
public class Post extends BaseTimeEntity {
//    long 대신 Long 사용 이유 => 기본 값이 null이므로 초기화시 0이되는 문제 방지

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CategoryType category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Post(Long id, CategoryType category, String title, String content, User user){
        this.category = category;
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Enum getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", user_id='" + user + '\'' +
                ", category=" + category +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

}

//@Entity
//@Table(name = "posts")
//public class Post extends BaseTimeEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long post_id;
//
//    @Column(length = 255, nullable = false)
//    private String post_title;
//
//    @Column(columnDefinition = "TEXT", nullable = false)
//    private String post_content;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    // 생성자, 게터, 세터 등 필요한 코드들...
//}