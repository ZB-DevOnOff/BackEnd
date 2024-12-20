package com.devonoff.config;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyCommentRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studyPost.repository.StudyReplyRepository;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final StudyPostRepository studyPostRepository;
  private final StudyCommentRepository studyCommentRepository;
  private final StudyReplyRepository studyReplyRepository;
  private final StudyPostService studyPostService;
  private final TimeProvider timeProvider;

  @Bean
  public Job deleteOldStudyPostsJob(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new JobBuilder("deleteOldStudyPostsJob", jobRepository)
        .start(deleteStep(jobRepository, transactionManager))
        .build();
  }

  @Bean
  public Step deleteStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new StepBuilder("deleteStep", jobRepository)
        .tasklet(deleteTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet deleteTasklet() {
    return (contribution, chunkContext) -> {
      LocalDateTime oneWeekAgo = timeProvider.now().minusDays(7);

      // 모집 기한이 지난 스터디 모집글을 CANCELED 로 변경(배치작업으로 자동 취소)
      studyPostService.cancelStudyPostIfExpired();

      List<StudyPost> studyPostsToDelete = studyPostRepository.findAllByStatusAndUpdatedAtBefore(
          StudyPostStatus.CANCELED, oneWeekAgo);

      // 댓글 / 대댓글 삭제
      for (StudyPost studyPost : studyPostsToDelete) {
        List<StudyComment> comments = studyCommentRepository.findAllByStudyPost(studyPost);

        for (StudyComment comment : comments) {
          studyReplyRepository.deleteAllByComment(comment);
        }

        studyCommentRepository.deleteAllByStudyPost(studyPost);
      }

      // 취소 상태에서 일주일이 지난 스터디 모집글 삭제
      studyPostRepository.deleteAll(studyPostsToDelete);

      return RepeatStatus.FINISHED;
    };
  }
}