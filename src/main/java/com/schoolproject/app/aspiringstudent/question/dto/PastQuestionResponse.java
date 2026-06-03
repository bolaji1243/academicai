package com.schoolproject.app.aspiringstudent.question.dto;

import com.schoolproject.app.aspiringstudent.dto.ExamTypeResponse;
import com.schoolproject.app.aspiringstudent.question.PastQuestion;
import com.schoolproject.app.aspiringstudent.subject.dto.SubjectResponse;
import com.schoolproject.app.aspiringstudent.topic.dto.TopicResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PastQuestionResponse {

    private String id;
    private ExamTypeResponse examType;
    private SubjectResponse subject;
    private TopicResponse topic;
    private Integer examYear;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;

    public static PastQuestionResponse from(PastQuestion question) {
        return new PastQuestionResponse(
                question.getPublicId(),
                ExamTypeResponse.from(question.getExamType()),
                SubjectResponse.from(question.getSubject()),
                TopicResponse.from(question.getTopic()),
                question.getExamYear(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectOption(),
                question.getExplanation()
        );
    }
}
