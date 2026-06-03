package com.schoolproject.app.aspiringstudent.examprofile;

import com.schoolproject.app.aspiringstudent.entity.ExamType;
import com.schoolproject.app.aspiringstudent.examprofile.dto.SaveStudentExamProfileRequest;
import com.schoolproject.app.aspiringstudent.examprofile.dto.StudentExamProfileResponse;
import com.schoolproject.app.aspiringstudent.repository.ExamTypeRepository;
import com.schoolproject.app.aspiringstudent.subject.Subject;
import com.schoolproject.app.aspiringstudent.subject.SubjectRepository;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.enums.Role;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentExamProfileService {

    private static final String ENGLISH_SLUG = "english-language";
    private static final String MATHEMATICS_SLUG = "mathematics";

    private final StudentExamProfileRepository studentExamProfileRepository;
    private final UserRepository userRepository;
    private final ExamTypeRepository examTypeRepository;
    private final SubjectRepository subjectRepository;

    public StudentExamProfileService(
            StudentExamProfileRepository studentExamProfileRepository,
            UserRepository userRepository,
            ExamTypeRepository examTypeRepository,
            SubjectRepository subjectRepository
    ) {
        this.studentExamProfileRepository = studentExamProfileRepository;
        this.userRepository = userRepository;
        this.examTypeRepository = examTypeRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public StudentExamProfileResponse createProfile(String email, SaveStudentExamProfileRequest request) {
        User user = findStudentUser(email);

        if (studentExamProfileRepository.existsByUser(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student exam profile already exists");
        }

        StudentExamProfile profile = new StudentExamProfile();
        profile.setUser(user);
        applyRequest(profile, request);

        return StudentExamProfileResponse.from(studentExamProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public StudentExamProfileResponse getProfile(String email) {
        User user = findStudentUser(email);

        return StudentExamProfileResponse.from(studentExamProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student exam profile not found")));
    }

    @Transactional
    public StudentExamProfileResponse updateProfile(String email, SaveStudentExamProfileRequest request) {
        User user = findStudentUser(email);
        StudentExamProfile profile = studentExamProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student exam profile not found"));

        applyRequest(profile, request);

        return StudentExamProfileResponse.from(studentExamProfileRepository.save(profile));
    }

    private void applyRequest(StudentExamProfile profile, SaveStudentExamProfileRequest request) {
        ExamType examType = examTypeRepository.findByPublicId(request.getExamTypeId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam type not found"));

        Set<String> subjectIds = cleanSubjectIds(request.getSubjectIds());
        List<Subject> subjects = subjectRepository.findByPublicIdIn(subjectIds);

        if (subjects.size() != subjectIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more subjects were not found");
        }

        validateExamRules(examType, subjects);

        profile.setExamType(examType);
        profile.setSubjects(new LinkedHashSet<>(subjects));
    }

    private User findStudentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.ASPIRING_STUDENT && user.getRole() != Role.UNIVERSITY_STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can create an exam profile");
        }

        return user;
    }

    private Set<String> cleanSubjectIds(List<String> rawSubjectIds) {
        Set<String> cleanedIds = rawSubjectIds.stream()
                .map(id -> id == null ? "" : id.trim())
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (cleanedIds.size() != rawSubjectIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject IDs must not be blank or duplicated");
        }

        return cleanedIds;
    }

    private void validateExamRules(ExamType examType, List<Subject> subjects) {
        String examSlug = examType.getSlug();
        int count = subjects.size();

        if ("jamb".equals(examSlug)) {
            requireSubjectCount(count, 4, "JAMB requires exactly 4 subjects");
            requireSubject(subjects, ENGLISH_SLUG, "JAMB requires English Language");
            return;
        }

        if ("waec".equals(examSlug) || "neco".equals(examSlug) || "gce".equals(examSlug)) {
            if (count < 8 || count > 9) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, examType.getName() + " requires 8 to 9 subjects");
            }

            requireSubject(subjects, ENGLISH_SLUG, examType.getName() + " requires English Language");
            requireSubject(subjects, MATHEMATICS_SLUG, examType.getName() + " requires Mathematics");
            return;
        }

        if ("post-utme".equals(examSlug)) {
            return;
        }

        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one subject is required");
        }
    }

    private void requireSubjectCount(int actualCount, int requiredCount, String message) {
        if (actualCount != requiredCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void requireSubject(List<Subject> subjects, String requiredSlug, String message) {
        boolean present = subjects.stream().anyMatch(subject -> requiredSlug.equals(subject.getSlug()));

        if (!present) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
