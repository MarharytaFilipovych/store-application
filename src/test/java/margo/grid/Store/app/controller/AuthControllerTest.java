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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static margo.grid.store.app.testdata.AuthTestDataProvider.createResetPasswordDto;
import static margo.grid.store.app.testdata.AuthTestDataProvider.createUserDto;
import static org.junit.jupiter.api.Assertions.*;
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
    private String incompleteJson;
    private String invalidJson;
    private ForgotPasswordDto forgotPasswordDto;

    @BeforeEach
    void setUp() {
        resetCode = UUID.randomUUID().toString();
        userDto = createUserDto();
        resetPasswordDto = createResetPasswordDto(UUID.fromString(resetCode));
        sessionId = UUID.randomUUID().toString();
        incompleteJson = "{\"email\":\"test@example.com\"}";
        invalidJson = "jjjj\"";
        forgotPasswordDto = new ForgotPasswordDto(userDto.getEmail());
    }

    @Test
    void register_withValidData_shouldRegisterUserAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(authService).register(userDto);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(authService).register(userDtoArgumentCaptor.capture());
        UserDto captured = userDtoArgumentCaptor.getValue();
        assertEquals(userDto.getEmail(), captured.getEmail());
        assertEquals(userDto.getPassword(), captured.getPassword());
    }

    @Test
    void register_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        userDto.setEmail("kkkk");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(userDto);
    }

    @Test
    void register_withInvalidPassword_shouldReturnBadRequest() throws Exception {
        // Arrange
        userDto.setPassword("....");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(userDto);
    }

    @Test
    void register_withExistingEmail_shouldReturnConflict() throws Exception {
        // Arrange
        doThrow(new UserAlreadyExistsException(userDto.getEmail()))
                .when(authService).register(userDto);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());

        verify(authService).register(userDto);
    }

    @Test
    void register_withInvalidJson_shouldReturnBadRequest() throws Exception {
         // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_withMissingFields_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).register(userDto);
    }

    @Test
    void login_withValidCredentials_shouldReturnSessionId() throws Exception {
        // Arrange
        when(authService.login(any(UserDto.class), any(HttpServletRequest.class)))
                .thenReturn(sessionId);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(sessionId));

        verify(authService).login(userDtoArgumentCaptor.capture(), any(HttpServletRequest.class));
        UserDto captured = userDtoArgumentCaptor.getValue();
        assertEquals(userDto.getEmail(), captured.getEmail());
        assertEquals(userDto.getPassword(), captured.getPassword());
    }

    @Test
    void login_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        userDto.setEmail(",,,");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).login(eq(userDto), any(HttpServletRequest.class));
    }

    @Test
    void login_withInvalidPassword_shouldReturnBadRequest() throws Exception {
        // Arrange
        userDto.setPassword(",,,");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).login(eq(userDto), any(HttpServletRequest.class));
    }

    @Test
    void login_withInvalidJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JSON format in request body"));

        verify(authService, never()).login(any(UserDto.class), any(HttpServletRequest.class));
    }

    @Test
    void forgotPassword_withValidEmail_shouldReturnResetCode() throws Exception {
        // Arrange
        when(authService.getResetCode(userDto.getEmail())).thenReturn(resetCode);

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(resetCode));

        verify(authService).getResetCode(emailArgumentCaptor.capture());
        assertEquals(userDto.getEmail(), emailArgumentCaptor.getValue());
    }

    @Test
    void forgotPassword_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        forgotPasswordDto.setEmail("..");

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordDto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).getResetCode(resetCode);
    }

    @Test
    void forgotPassword_withNonExistentEmail_shouldReturnNotFound() throws Exception {
        // Arrange
        when(authService.getResetCode(userDto.getEmail()))
                .thenThrow(new EntityNotFoundException("User with email " + userDto.getEmail() + " does not exist!"));

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordDto)))
                .andExpect(status().isNotFound());

        verify(authService).getResetCode(userDto.getEmail());
    }

    @Test
    void resetPassword_withValidData_shouldResetPasswordAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(authService).resetPassword(resetPasswordDto);

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(authService).resetPassword(resetPasswordDtoArgumentCaptor.capture());
        ResetPasswordDto captured = resetPasswordDtoArgumentCaptor.getValue();
        assertEquals(resetPasswordDto.getEmail(), captured.getEmail());
        assertEquals(resetPasswordDto.getResetCode(), captured.getResetCode());
        assertEquals(resetPasswordDto.getNewPassword(), captured.getNewPassword());
    }

    @Test
    void resetPassword_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        resetPasswordDto.setEmail("...");

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(resetPasswordDto);
    }

    @Test
    void resetPassword_withInvalidNewPassword_shouldReturnBadRequest() throws Exception {
        // Arrange
        resetPasswordDto.setNewPassword("...");

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).resetPassword(resetPasswordDto);
    }

    @Test
    void resetPassword_withNonExistentEmail_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User with email " + resetPasswordDto.getEmail() + " does not exist!"))
                .when(authService).resetPassword(any(ResetPasswordDto.class));

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andExpect(status().isNotFound());

        verify(authService).resetPassword(any());
    }

    @Test
    void resetPassword_withInvalidJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(resetPasswordDto);
    }

    @Test
    void resetPassword_withMissingFields_shouldReturnBadRequest() throws Exception {
         // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(resetPasswordDto);
    }
}