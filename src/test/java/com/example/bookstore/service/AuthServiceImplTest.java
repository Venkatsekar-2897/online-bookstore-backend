package com.example.bookstore.service;

import com.example.bookstore.dto.AuthDtos.AuthResponse;
import com.example.bookstore.dto.AuthDtos.LoginRequest;
import com.example.bookstore.dto.AuthDtos.RegisterRequest;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.TokenService;
import com.example.bookstore.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokens;
    @InjectMocks private AuthServiceImpl service;

    @Test
    void registerNormalizesEmailTrimsNameAndHashesPassword() {
        when(users.existsByEmail("reader@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.create("reader@example.com", "USER")).thenReturn("jwt-token");

        AuthResponse result = service.register(new RegisterRequest("  Reader One  ", " Reader@Example.com ", "password123"));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("reader@example.com");
        assertThat(captor.getValue().getFullName()).isEqualTo("Reader One");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerRejectsAnAlreadyRegisteredEmail() {
        when(users.existsByEmail("reader@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("Reader", "Reader@Example.com", "password123")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email is already registered");

        verify(users, never()).save(any());
        verifyNoInteractions(passwordEncoder, tokens);
    }

    @Test
    void loginNormalizesEmailAndReturnsTokenForValidCredentials() {
        AppUser user = new AppUser("reader@example.com", "hashed-password", "Reader");
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(tokens.create("reader@example.com", "USER")).thenReturn("jwt-token");

        AuthResponse result = service.login(new LoginRequest(" READER@EXAMPLE.COM ", "password123"));

        assertThat(result.email()).isEqualTo("reader@example.com");
        assertThat(result.fullName()).isEqualTo("Reader");
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(passwordEncoder).matches("password123", "hashed-password");
    }

    @Test
    void loginRejectsUnknownEmailWithGenericMessage() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "password123")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email or password");

        verifyNoInteractions(passwordEncoder, tokens);
    }

    @Test
    void loginRejectsIncorrectPassword() {
        AppUser user = new AppUser("reader@example.com", "hashed-password", "Reader");
        when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("reader@example.com", "wrong-password")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email or password");

        verifyNoInteractions(tokens);
    }
}
