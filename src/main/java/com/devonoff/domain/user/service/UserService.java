package com.devonoff.domain.user.service;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.dto.UserUpdateRequest;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

  private final AuthService authService;
  private final PhotoService photoService;

  private final UserRepository userRepository;

  /**
   * 유저 정보 상세 조회
   *
   * @param userId
   * @return UserDto
   */
  public UserDto getUserDetail(Long userId) {
    checkEqualUser(userId);

    return UserDto.fromEntity(userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
  }

  /**
   * 유저 정보 수정
   *
   * @param userId
   * @return
   */
  public UserDto updateUserInfo(Long userId, UserUpdateRequest userUpdateRequest) {
    checkEqualUser(userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    boolean isExists = userRepository.existsByNickname(userUpdateRequest.getNickname());
    if (isExists) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    }

    user.setNickname(userUpdateRequest.getNickname());

    return UserDto.fromEntity(userRepository.save(user));
  }

  /**
   * 프로필 이미지 수정
   *
   * @param userId
   * @return
   */
  public UserDto updateProfileImage(Long userId, MultipartFile profileImage) {
    checkEqualUser(userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String profileImageUrl = photoService.save(profileImage);

    user.setProfileImage(profileImageUrl);

    return UserDto.fromEntity(userRepository.save(user));
  }

  /**
   * 프로필 이미지 삭제
   *
   * @param userId
   */
  public void deleteProfileImage(Long userId) {
    checkEqualUser(userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    photoService.delete(user.getProfileImage());
  }

  /**
   * 요청한 유저 정보와 로그인한 유저의 정보가 일치하는지 확인
   *
   * @param userId
   */
  private void checkEqualUser(Long userId) {
    Long loginUserId = authService.getLoginUserId();

    if (!userId.equals(loginUserId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    }
  }

}