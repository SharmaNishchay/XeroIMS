package com.darkhex.xeroims.service;

import com.darkhex.xeroims.dto.UserDTO;
import com.darkhex.xeroims.model.User;
import com.darkhex.xeroims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return false;
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(user);
        return true;
    }

    // ✅ Add this method
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
