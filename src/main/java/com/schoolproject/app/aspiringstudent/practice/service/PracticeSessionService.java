package com.schoolproject.app.aspiringstudent.practice.service;
import com.schoolproject.app.aspiringstudent.entity.ExamType;
import com.schoolproject.app.aspiringstudent.practice.dto.PracticeResultResponse;
import com.schoolproject.app.aspiringstudent.practice.dto.PracticeSessionSummary;
import com.schoolproject.app.aspiringstudent.practice.dto.SubmitPracticeRequest;
import com.schoolproject.app.aspiringstudent.practice.entity.PracticeAnswer;
import com.schoolproject.app.aspiringstudent.practice.entity.PracticeSession;
import com.schoolproject.app.aspiringstudent.practice.repository.PracticeAnswerRepository;
import com.schoolproject.app.aspiringstudent.practice.repository.PracticeSessionRepository;
import com.schoolproject.app.aspiringstudent.question.PastQuestion;
import com.schoolproject.app.aspiringstudent.question.PastQuestionRepository;
import com.schoolproject.app.aspiringstudent.repository.ExamTypeRepository;
import com.schoolproject.app.aspiringstudent.streak.StudentStreakService;
import com.schoolproject.app.aspiringstudent.subject.Subject;
import com.schoolproject.app.aspiringstudent.subject.SubjectRepository;
import com.schoolproject.app.aspiringstudent.topic.Topic;
import com.schoolproject.app.aspiringstudent.topic.TopicRepository;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PracticeSessionService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;
    private final PastQuestionRepository pastQuestionRepository;
    private final ExamTypeRepository examTypeRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final StudentStreakService studentStreakService;

    public PracticeSessionService(
            PracticeSessionRepository practiceSessionRepository,
            PracticeAnswerRepository practiceAnswerRepository,
            PastQuestionRepository pastQuestionRepository,
            ExamTypeRepository examTypeRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            StudentStreakService studentStreakService
    ) {
        this.practiceSessionRepository = practiceSessionRepository;
        this.practiceAnswerRepository = practiceAnswerRepository;
        this.pastQuestionRepository = pastQuestionRepository;
        this.examTypeRepository = examTypeRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.studentStreakService = studentStreakService;
    }

    @Transactional
    public PracticeResultResponse submitSession(String username, SubmitPracticeRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ExamType examType = examTypeRepository.findByPublicId(request.examTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam type not found"));

        Subject subject = subjectRepository.findByPublicId(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        Topic topic = topicRepository.findByPublicId(request.topicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        // Load all submitted question IDs in one query
        List<String> submittedQuestionIds = request.answers().stream()
                .map(SubmitPracticeRequest.AnswerItem::questionId)
                .toList();

        List<PastQuestion> questions = pastQuestionRepository.findByPublicIdIn(submittedQuestionIds);

        if (questions.size() != submittedQuestionIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more question IDs are invalid");
        }

        // Map publicId -> PastQuestion for fast lookup
        Map<String, PastQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(PastQuestion::getPublicId, q -> q));

        // Build session
        PracticeSession session = new PracticeSession();
        session.setUser(user);
        session.setExamType(examType);
        session.setSubject(subject);
        session.setTopic(topic);
        session.setTotalQuestions(request.answers().size());

        // Grade answers
        int correct = 0;
        List<PracticeAnswer> practiceAnswers = new ArrayList<>();
        List<PracticeResultResponse.AnswerDetail> answerDetails = new ArrayList<>();

        for (SubmitPracticeRequest.AnswerItem item : request.answers()) {
            PastQuestion question = questionMap.get(item.questionId());
            boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(item.selectedOption());

            if (isCorrect) correct++;

            PracticeAnswer answer = new PracticeAnswer();
            answer.setSession(session);
            answer.setQuestion(question);
            answer.setSelectedOption(item.selectedOption());
            answer.setCorrect(isCorrect);
            practiceAnswers.add(answer);

            answerDetails.add(new PracticeResultResponse.AnswerDetail(
                    question.getPublicId(),
                    question.getQuestionText(),
                    item.selectedOption(),
                    question.getCorrectOption(),
                    isCorrect,
                    question.getExplanation()
            ));
        }

        session.setScore(correct);
        session.setAnswers(practiceAnswers);
        PracticeSession savedSession = practiceSessionRepository.save(session);
        studentStreakService.recordActivity(username, savedSession.getCreatedAt());

        return PracticeResultResponse.from(savedSession, answerDetails);
    }

    @Transactional(readOnly = true)
    public List<PracticeSessionSummary> getSessionsForUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return practiceSessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PracticeSessionSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PracticeResultResponse getSessionDetail(String username, String sessionId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PracticeSession session = practiceSessionRepository.findByPublicId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        List<PracticeResultResponse.AnswerDetail> answerDetails = session.getAnswers().stream()
                .map(a -> new PracticeResultResponse.AnswerDetail(
                        a.getQuestion().getPublicId(),
                        a.getQuestion().getQuestionText(),
                        a.getSelectedOption(),
                        a.getQuestion().getCorrectOption(),
                        a.isCorrect(),
                        a.getQuestion().getExplanation()
                ))
                .toList();

        return PracticeResultResponse.from(session, answerDetails);
    }
}
