package mongkey.maeilmail.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mongkey.maeilmail.common.response.ApiResponse;
import mongkey.maeilmail.common.response.Error;
import mongkey.maeilmail.common.response.Success;
import mongkey.maeilmail.config.jwt.JwtTokenProvider;
import mongkey.maeilmail.domain.Admin;
import mongkey.maeilmail.domain.User;
import mongkey.maeilmail.dto.token.TokenResponse;
import mongkey.maeilmail.dto.admin.request.JoinAdminRequestDto;
import mongkey.maeilmail.dto.admin.request.LoginAdminRequestDto;
import mongkey.maeilmail.dto.user.request.JoinUserRequestDto;
import mongkey.maeilmail.dto.user.request.LoginUserRequestDto;
import mongkey.maeilmail.repository.AdminRepository;
import mongkey.maeilmail.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    private final AdminRepository adminRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    /*유저 회원가입*/
    @Transactional
    public ApiResponse<?> joinUser(JoinUserRequestDto requestDto){
        Optional<User> findUser = userRepository.findByLoginId(requestDto.getLogin_id());

        //이미 가입된 유저
        if (findUser.isPresent()){
            return ApiResponse.failure(Error.ERROR, "이미 가입된 유저입니다");
        }

        User user = userRepository.save(requestDto.toEntity());
        return ApiResponse.success(Success.SUCCESS, user);
    }

    /*유저 로그인*/
    @Transactional
    public ApiResponse<?> loginUser(LoginUserRequestDto requestDto){
        Optional<User> findUser = userRepository.findByLoginId(requestDto.getLogin_id());

        //가입되지 않은 유저
        if (!findUser.isPresent()){
            return ApiResponse.failure(Error.ERROR, "가입되지 않은 유저입니다");
        }

        String savedPassword = findUser.get().getPassword();
        String inputPassword = requestDto.getPassword();

        if (!savedPassword.equals(inputPassword)){
            return ApiResponse.failure(Error.ERROR, "비밀번호가 틀렸습니다");
        }

        // 토큰 발급
        return ApiResponse.success(Success.SUCCESS, new TokenResponse(createToken(requestDto), "bearer", findUser.get().getId()));
    }

    /*관리자 회원가입*/
    @Transactional
    public ApiResponse<?> joinAdmin(JoinAdminRequestDto requestDto){

        Optional<Admin> findAdmin = adminRepository.findByEmployeeNumber(requestDto.getEmployee_number());

        //이미 가입된 관리자
        if (findAdmin.isPresent()){
            return ApiResponse.failure(Error.ERROR, "이미 등록된 관리자입니다");
        }

        Admin admin = adminRepository.save(requestDto.toEntity());
        return ApiResponse.success(Success.SUCCESS, admin);
    }

    /*관리자 로그인*/
    @Transactional
    public ApiResponse<?> loginAdmin(LoginAdminRequestDto requestDto){
        Optional<Admin> findAdmin = adminRepository.findByEmployeeNumber(requestDto.getEmployee_number());

        //관리자가 아닌 경우
        if (!findAdmin.isPresent()){
            return ApiResponse.failure(Error.ERROR, "관리자가 아닙니다");
        }

        String savedPassword = findAdmin.get().getPassword();
        String inputPassword = requestDto.getPassword();

        if (!savedPassword.equals(inputPassword)){
            return ApiResponse.failure(Error.ERROR, "비밀번호가 틀렸습니다");
        }

        return ApiResponse.success(Success.SUCCESS, findAdmin);
    }

    /*토큰 발행*/
    public String createToken(LoginUserRequestDto requestDto) {
        Optional<User> findUser = userRepository.findByLoginId(requestDto.getLogin_id());
        findUser.orElseThrow(IllegalArgumentException::new);
        return jwtTokenProvider.createToken(findUser.get().getName());
    }
}
