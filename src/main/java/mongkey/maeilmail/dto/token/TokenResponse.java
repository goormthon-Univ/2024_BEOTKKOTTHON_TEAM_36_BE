package mongkey.maeilmail.dto.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
//@NoArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private Long id;

    public TokenResponse(String accessToken, String tokenType, Long id) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.id = id;
    }
}
