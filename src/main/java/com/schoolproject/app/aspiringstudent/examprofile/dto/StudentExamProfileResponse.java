package com.schoolproject.app.aspiringstudent.examprofile.dto;

import com.schoolproject.app.aspiringstudent.dto.ExamTypeResponse;
import com.schoolproject.app.aspiringstudent.examprofile.StudentExamProfile;
import com.schoolproject.app.aspiringstudent.subject.dto.SubjectResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
@AllArgsConstructor
public class StudentExamProfileResponse {

    private String id;
    private ExamTypeResponse examType;
    private List<SubjectResponse> subjects;

    public static StudentExamProfileResponse from(StudentExamProfile profile) {
        return new StudentExamProfileResponse(
                profile.getPublicId(),
                ExamTypeResponse.from(profile.getExamType()),
                profile.getSubjects().stream()
                        .sorted(Comparator.comparing(subject -> subject.getName().toLowerCase()))
                        .map(SubjectResponse::from)
                        .toList()
        );
    }
}
