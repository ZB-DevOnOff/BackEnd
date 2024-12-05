package com.devonoff.domain.studyPost.service;

import static org.mockito.Mockito.when;

import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StudyPostServiceTest {

  @Mock
  private StudyPostRepository studyPostRepository;

  @InjectMocks
  private StudyPostService studyPostService;

  @DisplayName("스터디 모집글 상세 조회 성공")
  @Test
  void getStudyPostDetail_Success() {
    // Given
    Long studyPostId = 1L;

    User user = new User();
    user.setId(11L);

    StudyPost studyPost = new StudyPost();
    studyPost.setId(studyPostId);
    studyPost.setTitle("스터디 모집글! 상세 조회 테스트");
    studyPost.setStudyName("코테");
    studyPost.setSubject(StudySubject.JOB_PREPARATION);
    studyPost.setDifficulty(StudyDifficulty.HIGH);
    studyPost.setDayType(3);
    studyPost.setStartDate(LocalDate.parse("2024-12-04"));
    studyPost.setEndDate(LocalDate.parse("2024-12-22"));
    studyPost.setStartTime(LocalTime.parse("19:00"));
    studyPost.setEndTime(LocalTime.parse("21:00"));
    studyPost.setMeetingType(StudyMeetingType.HYBRID);
    studyPost.setRecruitmentPeriod(LocalDate.parse("2024-11-30"));
    studyPost.setDescription("코테 공부할사람 모여");
    studyPost.setLatitude(35.6895);
    studyPost.setLongitude(139.6917);
    studyPost.setMaxParticipants(5);
    studyPost.setUser(user);

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));

    // When
    StudyPostDto result = studyPostService.getStudyPostDetail(studyPostId);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(studyPostId, result.getId());
    Assertions.assertEquals("스터디 모집글! 상세 조회 테스트", result.getTitle());
    Assertions.assertEquals("코테", result.getStudyName());
    Assertions.assertEquals(StudySubject.JOB_PREPARATION, result.getSubject());
    Assertions.assertEquals(StudyDifficulty.HIGH, result.getDifficulty());
    Assertions.assertEquals(List.of("월", "화"), result.getDayType());
    Assertions.assertEquals(LocalDate.parse("2024-12-04"), result.getStartDate());
    Assertions.assertEquals(LocalDate.parse("2024-12-22"), result.getEndDate());
    Assertions.assertEquals(LocalTime.parse("19:00"), result.getStartTime());
    Assertions.assertEquals(LocalTime.parse("21:00"), result.getEndTime());
    Assertions.assertEquals(StudyMeetingType.HYBRID, result.getMeetingType());
    Assertions.assertEquals(LocalDate.parse("2024-11-30"), result.getRecruitmentPeriod());
    Assertions.assertEquals("코테 공부할사람 모여", result.getDescription());
    Assertions.assertEquals(35.6895, result.getLatitude());
    Assertions.assertEquals(139.6917, result.getLongitude());
    Assertions.assertEquals(5, result.getMaxParticipants());
    Assertions.assertEquals(11L, result.getUserId());
  }

  @DisplayName("스터디 모집글 상세 조회 실패")
  @Test
  void getStudyPostDetail_NotFound() {
    // Given
    Long studyPostId = 123L;

    // Optional.empty()를 반환하도록 설정
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = Assertions.assertThrows(CustomException.class,
        () -> studyPostService.getStudyPostDetail(studyPostId));

    Assertions.assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("스터디 모집글 검색 성공")
  @Test
  void searchStudyPosts_Success() {
    // Given
    StudyMeetingType meetingType = StudyMeetingType.ONLINE;
    String title = "코테";
    StudySubject subject = StudySubject.JOB_PREPARATION;
    StudyDifficulty difficulty = StudyDifficulty.MEDIUM;
    int dayType = 3; // 월, 화
    StudyPostStatus status = StudyPostStatus.RECRUITING;
    Double latitude = 37.5665;
    Double longitude = 126.9780;
    Pageable pageable = PageRequest.of(0, 20);

    // 데이터 생성
    StudyPostDto studyPostDto = new StudyPostDto();
    studyPostDto.setId(1L);
    studyPostDto.setTitle("코딩 테스트 준비");
    studyPostDto.setStudyName("코테");
    studyPostDto.setSubject(StudySubject.JOB_PREPARATION);
    studyPostDto.setDifficulty(StudyDifficulty.MEDIUM);

    Page<StudyPostDto> mockPage = new PageImpl<>(List.of(studyPostDto), pageable, 1);

    // When
    Mockito.when(
            studyPostRepository.findStudyPostsByFilters(Mockito.eq(meetingType), Mockito.eq(title),
                Mockito.eq(subject), Mockito.eq(difficulty), Mockito.eq(dayType), Mockito.eq(status),
                Mockito.eq(latitude), Mockito.eq(longitude), Mockito.eq(pageable)))
        .thenReturn(mockPage);

    // When
    Page<StudyPostDto> result = studyPostService.searchStudyPosts(meetingType, title, subject,
        difficulty, dayType, status, latitude, longitude, pageable);

    // Then
    Assertions.assertNotNull(result, "Result should not be null");
    Assertions.assertEquals(1, result.getTotalElements(), "Total elements should match");
    Assertions.assertEquals("코딩 테스트 준비", result.getContent().get(0).getTitle(),
        "Title should match");
    Assertions.assertEquals("코테", result.getContent().get(0).getStudyName(),
        "Study name should match");
    Assertions.assertEquals(StudySubject.JOB_PREPARATION, result.getContent().get(0).getSubject(),
        "Subject should match");
    Assertions.assertEquals(StudyDifficulty.MEDIUM, result.getContent().get(0).getDifficulty(),
        "Difficulty should match");
  }
}