package com.nutrition.tracker.controller;

import com.nutrition.tracker.dto.UserProfileRequest;
import com.nutrition.tracker.entity.User;
import com.nutrition.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UserProfileRequest request) {
        User updatedUser = userService.updateProfile(request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/recommended-calories")
    public ResponseEntity<Integer> getRecommendedCalories() {
        User user = userService.getCurrentUser();
        Integer calories = userService.calculateRecommendedCalories(user);
        return ResponseEntity.ok(calories);
    }
}
