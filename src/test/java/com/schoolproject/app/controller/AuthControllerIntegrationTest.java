package com.schoolproject.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolproject.app.dto.LoginRequest;
import com.schoolproject.app.dto.RegisterRequest;
import com.schoolproject.app.enums.Role;
import com.schoolproject.app.repository.LecturerProfileRepository;
import com.schoolproject.app.repository.UniversityStudentProfileRepository;
import com.schoolproject.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LecturerProfileRepository lecturerProfileRepository;

    @Autowired
    private UniversityStudentProfileRepository universityStudentProfileRepository;

    @Test
    void registerReturnsSuccessMessage() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully. Check your email to verify your account")));
    }

    @Test
    void registerCreatesUniversityStudentWhenRequested() throws Exception {
        Map<String, String> request = Map.of(
                "fullName", "Grace Hopper",
                "email", "grace@example.com",
                "password", "password123",
                "role", "UNIVERSITY_STUDENT",
                "matricNumber", "MAT-001",
                "department", "Computer Science",
                "level", "300",
                "faculty", "Science"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByEmail("grace@example.com")
                .filter(user -> user.getRole() == Role.UNIVERSITY_STUDENT)
                .isPresent());
        assertTrue(universityStudentProfileRepository.existsByMatricNumber("MAT-001"));
    }

    @Test
    void registerRejectsLecturerRole() throws Exception {
        Map<String, String> request = Map.of(
                "fullName", "Mary Jackson",
                "email", "mary@example.com",
                "password", "password123",
                "role", "LECTURER"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Student role must be ASPIRING_STUDENT or UNIVERSITY_STUDENT")));
    }

    @Test
    void registerLecturerCreatesLecturerWithValidCode() throws Exception {
        Map<String, String> request = Map.of(
                "fullName", "Katherine Johnson",
                "email", "katherine@example.com",
                "password", "password123",
                "department", "Mathematics",
                "faculty", "Science",
                "staffId", "STAFF-001",
                "lecturerRegistrationCode", "test-lecturer-code"
        );

        mockMvc.perform(post("/api/auth/register-lecturer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Lecturer registered successfully")))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role", is("LECTURER")));

        assertTrue(userRepository.findByEmail("katherine@example.com")
                .filter(user -> user.getRole() == Role.LECTURER && user.isEnabled())
                .isPresent());
        assertTrue(lecturerProfileRepository.existsByStaffId("STAFF-001"));
    }

    @Test
    void registeredLecturerCanAccessDashboardWithSignupToken() throws Exception {
        Map<String, String> request = Map.of(
                "fullName", "Evelyn Boyd Granville",
                "email", "evelyn@example.com",
                "password", "password123",
                "department", "Mathematics",
                "faculty", "Science",
                "staffId", "STAFF-003",
                "lecturerRegistrationCode", "test-lecturer-code"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register-lecturer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        mockMvc.perform(get("/api/lecturer/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCourses", is(0)))
                .andExpect(jsonPath("$.data.totalStudents", is(0)))
                .andExpect(jsonPath("$.data.pendingSubmissions", is(0)))
                .andExpect(jsonPath("$.data.recentAnnouncements").isArray())
                .andExpect(jsonPath("$.data.courseSummaries").isArray());
    }

    @Test
    void registerLecturerRejectsInvalidCode() throws Exception {
        Map<String, String> request = Map.of(
                "fullName", "Dorothy Vaughan",
                "email", "dorothy@example.com",
                "password", "password123",
                "department", "Mathematics",
                "faculty", "Science",
                "staffId", "STAFF-002",
                "lecturerRegistrationCode", "wrong-code"
        );

        mockMvc.perform(post("/api/auth/register-lecturer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Invalid lecturer registration code")));
    }

    @Test
    void loginValidationRejectsInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.validationErrors.email", is("Email must be valid")));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }
}
