package margo.grid.store.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import margo.grid.store.app.dto.ForgotPasswordDto;
import margo.grid.store.app.dto.ResetPasswordDto;
import margo.grid.store.app.dto.UserDto;
import margo.grid.store.app.exception.UserAlreadyExistsException;
import margo.grid.store.app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;
import java.util.stream.Stream;
import static margo.grid.store.app.testdata.AuthTestDataProvider.createResetPasswordDto;
import static margo.grid.store.app.testdata.AuthTestDataProvider.createUserDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Captor
    private ArgumentCaptor<UserDto> userDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> emailArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResetPasswordDto> resetPasswordDtoArgumentCaptor;

    private UserDto userDto;
    private ResetPasswordDto resetPasswordDto;
    private String sessionId;
    private String resetCode;
    private ForgotPasswordDto forgotPasswordDto;

    @BeforeEach
    void setUp() {
        resetCode = UUID.randomUUID().toString();
        userDto = createUserDto();
        resetPasswordDto = createResetPasswordDto(UUID.fromString(resetCode));
        sessionId = UUID.randomUUID().toString();
        forgotPasswordDto = new ForgotPasswordDto(userDto.getEmail());
    }

    @Test
    void register_withValidData_shouldRegisterUserAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(authService).register(userDto);

        // Act & Assert
        performRegisterRequest(userDto).andExpect(status().isOk());

        verifyRegisterCaptureAndAssert();
    }

    @ParameterizedTest
    @ValueSource(strings = {"kkkk", "invalid", "notanemail", "@missing.com"})
    void register_withInvalidEmail_shouldReturnBadRequest(String invalidEmail) throws Exception {
        // Arrange
        userDto.setEmail(invalidEmail);

        // Act & Assert
        performRegisterRequest(userDto).andExpect(status().isBadRequest());

        verify(authService, never()).register(userDto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"....", "weak", "12345", "short"})
    void register_withInvalidPassword_shouldReturnBadRequest(String invalidPassword) throws Exception {
        // Arrange
        userDto.setPassword(invalidPassword);

        // Act & Assert
        performRegisterRequest(userDto).andExpect(status().isBadRequest());

        verify(authService, never()).register(userDto);
    }

    @Test
    void register_withExistingEmail_shouldReturnConflict() throws Exception {
        // Arrange
        doThrow(new UserAlreadyExistsException(userDto.getEmail()))
                .when(authService).register(userDto);

        // Act & Assert
        performRegisterRequest(userDto).andExpect(status().isConflict());

        verify(authService).register(userDto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"jjjj\"", "{\"email\":\"test@example.com\"}", "{}", "", "null"})
    void register_withInvalidJson_shouldReturnBadRequest(String json) throws Exception {
        // Act & Assert
        performRegisterRequestWithRawJson(json).andExpect(status().isBadRequest());

        verify(authService, never()).register(userDto);
    }

    @Test
    void login_withValidCredentials_shouldReturnSessionId() throws Exception {
        // Arrange
        when(authService.login(any(UserDto.class), any(HttpServletRequest.class)))
                .thenReturn(sessionId);

        // Act & Assert
        performLoginRequest(userDto)
                .andExpect(status().isOk())
                .andExpect(content().string(sessionId));

        verifyLoginCaptureAndAssert();
    }

    @ParameterizedTest
    @ValueSource(strings = {",,,", "invalid", "@test", "bad.email"})
    void login_withInvalidEmail_shouldReturnBadRequest(String invalidEmail) throws Exception {
        // Arrange
        userDto.setEmail(invalidEmail);

        // Act & Assert
        performLoginRequest(userDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).login(eq(userDto), any(HttpServletRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {",,,", "bad", "xyz"})
    void login_withInvalidPassword_shouldReturnBadRequest(String invalidPassword) throws Exception {
        // Arrange
        userDto.setPassword(invalidPassword);

        // Act & Assert
        performLoginRequest(userDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).login(eq(userDto), any(HttpServletRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jjjj\"", "{\"email\":\"test@example.com\"}", "{}", "", "null"})
    void login_withInvalidJson_shouldReturnBadRequest(String json) throws Exception {
        // Act & Assert
        performLoginRequestWithRawJson(json).andExpect(status().isBadRequest());

        verify(authService, never()).login(eq(userDto), any(HttpServletRequest.class));
    }

    @Test
    void forgotPassword_withValidEmail_shouldReturnResetCode() throws Exception {
        // Arrange
        when(authService.getResetCode(userDto.getEmail())).thenReturn(resetCode);

        // Act & Assert
        performForgotPasswordRequest(forgotPasswordDto)
                .andExpect(status().isOk())
                .andExpect(content().string(resetCode));

        verifyForgotPasswordCaptureAndAssert();
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "invalid", "notanemail"})
    void forgotPassword_withInvalidEmail_shouldReturnBadRequest(String invalidEmail) throws Exception {
        // Arrange
        forgotPasswordDto.setEmail(invalidEmail);

        // Act & Assert
        performForgotPasswordRequest(forgotPasswordDto).andExpect(status().isBadRequest());

        verify(authService, never()).getResetCode(userDto.getEmail());
    }

    @Test
    void forgotPassword_withNonExistentEmail_shouldReturnNotFound() throws Exception {
        // Arrange
        when(authService.getResetCode(userDto.getEmail()))
                .thenThrow(new EntityNotFoundException("User with email " + userDto.getEmail() + " does not exist!"));

        // Act & Assert
        performForgotPasswordRequest(forgotPasswordDto).andExpect(status().isNotFound());

        verify(authService).getResetCode(userDto.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jjjj\"", "{\"email:\"test@example.com\"}", "{}", "", "null"})
    void forgotPassword_withInvalidJson_shouldReturnBadRequest(String json) throws Exception {
        // Act & Assert
        performForgotPasswordRequestWithRawJson(json).andExpect(status().isBadRequest());

        verify(authService, never()).getResetCode(userDto.getEmail());
    }

    @Test
    void resetPassword_withValidData_shouldResetPasswordAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(authService).resetPassword(resetPasswordDto);

        // Act & Assert
        performResetPasswordRequest(resetPasswordDto).andExpect(status().isOk());

        verifyResetPasswordCaptureAndAssert();
    }

    @ParameterizedTest
    @ValueSource(strings = {"...", "invalid", "notanemail"})
    void resetPassword_withInvalidEmail_shouldReturnBadRequest(String invalidEmail) throws Exception {
        // Arrange
        resetPasswordDto.setEmail(invalidEmail);

        // Act & Assert
        performResetPasswordRequest(resetPasswordDto).andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"...", "weak", "bad"})
    void resetPassword_withInvalidNewPassword_shouldReturnBadRequest(String invalidPassword) throws Exception {
        // Arrange
        resetPasswordDto.setNewPassword(invalidPassword);

        // Act & Assert
        performResetPasswordRequest(resetPasswordDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).resetPassword(any());
    }

    @Test
    void resetPassword_withNonExistentEmail_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User with email " + resetPasswordDto.getEmail() + " does not exist!"))
                .when(authService).resetPassword(any(ResetPasswordDto.class));

        // Act & Assert
        performResetPasswordRequest(resetPasswordDto).andExpect(status().isNotFound());

        verify(authService).resetPassword(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jjjj\"", "{\"email\":\"test@example.com\"}", "{}", "", "null"})
    void resetPassword_withInvalidJson_shouldReturnBadRequest(String json) throws Exception {
        // Act & Assert
        performResetPasswordRequestWithRawJson(json).andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any());
    }

    private ResultActions performRegisterRequest(UserDto requestBody) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performRegisterRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performLoginRequest(UserDto requestBody) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performLoginRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performForgotPasswordRequest(ForgotPasswordDto requestBody) throws Exception {
        return mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performForgotPasswordRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performResetPasswordRequest(ResetPasswordDto requestBody) throws Exception {
        return mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    private ResultActions performResetPasswordRequestWithRawJson(String json) throws Exception {
        return mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private void verifyRegisterCaptureAndAssert() {
        verify(authService).register(userDtoArgumentCaptor.capture());
        UserDto captured = userDtoArgumentCaptor.getValue();
        assertEquals(userDto.getEmail(), captured.getEmail());
        assertEquals(userDto.getPassword(), captured.getPassword());
    }

    private void verifyLoginCaptureAndAssert() {
        verify(authService).login(userDtoArgumentCaptor.capture(), any(HttpServletRequest.class));
        UserDto captured = userDtoArgumentCaptor.getValue();
        assertEquals(userDto.getEmail(), captured.getEmail());
        assertEquals(userDto.getPassword(), captured.getPassword());
    }

    private void verifyForgotPasswordCaptureAndAssert() {
        verify(authService).getResetCode(emailArgumentCaptor.capture());
        assertEquals(userDto.getEmail(), emailArgumentCaptor.getValue());
    }

    private void verifyResetPasswordCaptureAndAssert() {
        verify(authService).resetPassword(resetPasswordDtoArgumentCaptor.capture());
        ResetPasswordDto captured = resetPasswordDtoArgumentCaptor.getValue();
        assertEquals(resetPasswordDto.getEmail(), captured.getEmail());
        assertEquals(resetPasswordDto.getResetCode(), captured.getResetCode());
        assertEquals(resetPasswordDto.getNewPassword(), captured.getNewPassword());
    }
}