package com.matt.iam.controllerTests;

import com.matt.iam.dtos.request.LoginRequest;
import com.matt.iam.dtos.request.RefreshTokenRequest;
import com.matt.iam.dtos.request.RegisterRequest;
import com.matt.iam.repositories.BlacklistedTokenRepository;
import com.matt.iam.repositories.RefreshTokenRepository;
import com.matt.iam.repositories.RoleRepository;
import com.matt.iam.repositories.UserRepository;
import com.matt.iam.services.AuthService;
import com.matt.iam.utils.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private static int TEST_MAX_LOGIN_ATTEMPTS = 5;
    private static int TEST_LOCK_TIME_MINUTES = 30;
    private static int TEST_MAX_REFRESH_TOKENS = 5;
    private RegisterRequest registerRequestTest;
    private LoginRequest loginRequestTest;
    private RefreshTokenRequest refreshRequestTest;
    private RefreshTokenRequest logoutRequestTest;

    @BeforeAll
    static void setup() {
        
    }
}
