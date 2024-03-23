package mongkey.maeilmail.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Success {

    // Default
    SUCCESS(HttpStatus.OK, "Request successfully processed"),


    //200 SUCCESS
    GET_ONE_POST_SUCCESS(HttpStatus.OK, "하나의 게시글을 성공적으로 조회하였습니다."),
    DELETE_POST_SUCCESS(HttpStatus.OK, "게시글을 성공적으로 삭제하였습니다."),

    //201 CREATED SUCCESS
    CREATE_POST_SUCCESS(HttpStatus.CREATED, "게시글을 성공적으로 등록하였습니다."),
    CREATE_EMAIL_SUCCESS(HttpStatus.CREATED, "성공적으로 이메일 전문을 생성하였습니다."),
    RECREATE_EMAIL_SUCCESS(HttpStatus.CREATED, "성공적으로 이메일을 재생성하였습니다."),
    ;


    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}