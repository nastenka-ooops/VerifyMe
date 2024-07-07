package com.example.authproject.service;

import com.example.authproject.dto.UserDto;
import com.example.authproject.entity.AppUser;
import com.example.authproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLoginIgnoreCase(username).orElseThrow(() ->
                new UsernameNotFoundException("user not found"));
    }

    public AppUser findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new UsernameNotFoundException("user not found"));
    }

    public UserDto getUser(String username) {
        AppUser user = loadUserByUsername(username);
        return new UserDto(user.getEmail(), user.getUsername());
    }

    public void confirmUser(AppUser user) {
        user.setIsConfirm(true);
        userRepository.save(user);
    }

    public void deleteAllUsers(){
        userRepository.deleteAll();
    }
}
