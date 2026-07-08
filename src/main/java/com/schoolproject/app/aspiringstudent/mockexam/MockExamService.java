package com.schoolproject.app.aspiringstudent.mockexam;

import com.schoolproject.app.aspiringstudent.examprofile.StudentExamProfile;
import com.schoolproject.app.aspiringstudent.examprofile.StudentExamProfileRepository;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamHistoryResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamResultResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamStartResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamSubmitRequest;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamAnswer;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSession;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSessionQuestion;
import com.schoolproject.app.aspiringstudent.question.PastQuestion;
import com.schoolproject.app.aspiringstudent.question.PastQuestionRepository;
import com.schoolproject.app.aspiringstudent.streak.StudentStreakService;
import com.schoolproject.app.aspiringstudent.subject.Subject;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MockExamService {

    private static final int JAMB_TOTAL_QUESTIONS = 60;
    private static final int JAMB_DURATION_MINUTES = 120;
    private static final int WAEC_TOTAL_QUESTIONS = 100;
    private static final int WAEC_MINUTES_PER_SUBJECT = 20;

    private final MockExamRepository mockExamRepository;
    private final StudentExamProfileRepository studentExamProfileRepository;
    private final PastQuestionRepository pastQuestionRepository;
    private final UserRepository userRepository;
    private final StudentStreakService studentStreakService;

    public MockExamService(
            MockExamRepository mockExamRepository,
            StudentExamProfileRepository studentExamProfileRepository,
            PastQuestionRepository pastQuestionRepository,
            UserRepository userRepository,
            StudentStreakService studentStreakService
    ) {
        this.mockExamRepository = mockExamRepository;
        this.studentExamProfileRepository = studentExamProfileRepository;
        this.pastQuestionRepository = pastQuestionRepository;
        this.userRepository = userRepository;
        this.studentStreakService = studentStreakService;
    }

    @Transactional
    public MockExamStartResponse startMockExam(String email) {
        User user = findUser(email);
        StudentExamProfile profile = studentExamProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student exam profile not found"));

        List<Subject> subjects = profile.getSubjects().stream()
                .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (subjects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student exam profile has no subjects");
        }

        String examSlug = profile.getExamType().getSlug();
        int totalQuestions = resolveTotalQuestions(examSlug);
        int durationMinutes = resolveDurationMinutes(examSlug, subjects.size());

        List<PastQuestion> selectedQuestions = selectQuestions(profile.getExamType(), subjects, totalQuestions);

        MockExamSession session = new MockExamSession();
        session.setUser(user);
        session.setExamType(profile.getExamType());
        session.getSubjects().addAll(subjects);
        session.setStartTime(LocalDateTime.now());
        session.setDurationMinutes(durationMinutes);
        session.setTotalQuestions(selectedQuestions.size());
        session.setStatus(MockExamStatus.IN_PROGRESS);

        for (int index = 0; index < selectedQuestions.size(); index++) {
            MockExamSessionQuestion question = new MockExamSessionQuestion();
            question.setSession(session);
            question.setQuestion(selectedQuestions.get(index));
            question.setQuestionOrder(index + 1);
            session.getQuestions().add(question);
        }

        return MockExamStartResponse.from(mockExamRepository.save(session));
    }

    @Transactional
    public MockExamResultResponse submitMockExam(String email, String sessionId, MockExamSubmitRequest request) {
        MockExamSession session = mockExamRepository.findByPublicIdAndUser_Email(sessionId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mock exam session not found"));

        if (session.getStatus() != MockExamStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mock exam session has already been submitted");
        }

        Map<String, MockExamSubmitRequest.AnswerItem> submittedAnswers = request.answers().stream()
                .collect(Collectors.toMap(
                        MockExamSubmitRequest.AnswerItem::questionId,
                        answer -> answer,
                        (left, right) -> {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate question answers detected");
                        },
                        LinkedHashMap::new
                ));

        if (submittedAnswers.size() != session.getQuestions().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All questions must be answered");
        }

        Map<String, MockExamSessionQuestion> sessionQuestions = session.getQuestions().stream()
                .collect(Collectors.toMap(
                        question -> question.getQuestion().getPublicId(),
                        question -> question,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        if (!sessionQuestions.keySet().containsAll(submittedAnswers.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more question IDs are invalid");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean timedOut = now.isAfter(session.getStartTime().plusMinutes(session.getDurationMinutes()));

        session.getAnswers().clear();

        int score = 0;
        for (Map.Entry<String, MockExamSubmitRequest.AnswerItem> entry : submittedAnswers.entrySet()) {
            MockExamSessionQuestion sessionQuestion = sessionQuestions.get(entry.getKey());
            PastQuestion question = sessionQuestion.getQuestion();
            boolean correct = question.getCorrectOption().equalsIgnoreCase(entry.getValue().selectedOption());

            if (correct) {
                score++;
            }

            MockExamAnswer answer = new MockExamAnswer();
            answer.setSession(session);
            answer.setQuestion(question);
            answer.setSelectedOption(entry.getValue().selectedOption());
            answer.setCorrect(correct);
            session.getAnswers().add(answer);
        }

        session.setScore(score);
        session.setEndTime(now);
        session.setStatus(timedOut ? MockExamStatus.TIMED_OUT : MockExamStatus.COMPLETED);

        MockExamSession savedSession = mockExamRepository.save(session);
        studentStreakService.recordActivity(email, savedSession.getEndTime());

        return MockExamResultResponse.from(savedSession);
    }

    @Transactional(readOnly = true)
    public List<MockExamHistoryResponse> getHistory(String email) {
        User user = findUser(email);

        return mockExamRepository.findHistoryByUser(user).stream()
                .map(history -> new MockExamHistoryResponse(
                        history.getSessionId(),
                        history.getStartedAt(),
                        history.getEndedAt(),
                        history.getExamType(),
                        history.getStatus(),
                        history.getScore(),
                        history.getTotalQuestions(),
                        calculateDurationTakenMinutes(history.getStartedAt(), history.getEndedAt())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MockExamResultResponse getResult(String email, String sessionId) {
        MockExamSession session = mockExamRepository.findByPublicIdAndUser_Email(sessionId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mock exam session not found"));

        if (session.getStatus() == MockExamStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mock exam session is still in progress");
        }

        return MockExamResultResponse.from(session);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private int resolveTotalQuestions(String examSlug) {
        if ("jamb".equals(examSlug)) {
            return JAMB_TOTAL_QUESTIONS;
        }

        return WAEC_TOTAL_QUESTIONS;
    }

    private int resolveDurationMinutes(String examSlug, int subjectCount) {
        if ("jamb".equals(examSlug)) {
            return JAMB_DURATION_MINUTES;
        }

        return Math.max(60, subjectCount * WAEC_MINUTES_PER_SUBJECT);
    }

    private List<PastQuestion> selectQuestions(com.schoolproject.app.aspiringstudent.entity.ExamType examType, List<Subject> subjects, int totalQuestions) {
        Map<Subject, List<PastQuestion>> questionBank = new LinkedHashMap<>();

        for (Subject subject : subjects) {
            List<PastQuestion> questions = pastQuestionRepository.findByExamTypeAndSubject(examType, subject);
            questionBank.put(subject, shuffleCopy(questions));
        }

        int subjectCount = subjects.size();
        int baseQuota = totalQuestions / subjectCount;
        int remainder = totalQuestions % subjectCount;

        Set<Long> selectedIds = new LinkedHashSet<>();
        List<PastQuestion> selected = new ArrayList<>();
        List<PastQuestion> overflow = new ArrayList<>();

        for (int index = 0; index < subjects.size(); index++) {
            Subject subject = subjects.get(index);
            List<PastQuestion> pool = questionBank.getOrDefault(subject, List.of());
            int quota = baseQuota + (index < remainder ? 1 : 0);
            int take = Math.min(quota, pool.size());

            for (int i = 0; i < take; i++) {
                PastQuestion question = pool.get(i);
                if (selectedIds.add(question.getId())) {
                    selected.add(question);
                }
            }

            if (pool.size() > take) {
                overflow.addAll(pool.subList(take, pool.size()));
            }
        }

        if (selected.size() < totalQuestions) {
            List<PastQuestion> shuffledOverflow = shuffleCopy(overflow);
            for (PastQuestion question : shuffledOverflow) {
                if (selectedIds.add(question.getId())) {
                    selected.add(question);
                }
                if (selected.size() == totalQuestions) {
                    break;
                }
            }
        }

        if (selected.size() < totalQuestions) {
            int available = selected.size();
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough questions available for " + examType.getName()
                            + ". Required " + totalQuestions
                            + ", found " + available
                            + " for subjects " + subjects.stream().map(Subject::getName).toList()
                            + ". Seed more past questions for this exam type."
            );
        }

        return shuffleCopy(selected).stream()
                .limit(totalQuestions)
                .toList();
    }

    private List<PastQuestion> shuffleCopy(List<PastQuestion> questions) {
        List<PastQuestion> copy = new ArrayList<>(questions);
        java.util.Collections.shuffle(copy);
        return copy;
    }

    private long calculateDurationTakenMinutes(LocalDateTime startedAt, LocalDateTime endedAt) {
        if (startedAt == null || endedAt == null) {
            return 0;
        }

        return Math.max(0, ChronoUnit.MINUTES.between(startedAt, endedAt));
    }
}
