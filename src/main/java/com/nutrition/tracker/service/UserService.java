package com.nutrition.tracker.service;

import com.nutrition.tracker.dto.UserProfileRequest;
import com.nutrition.tracker.entity.User;
import com.nutrition.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(UserProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }
        if (request.getDailyCalorieGoal() != null) {
            user.setDailyCalorieGoal(request.getDailyCalorieGoal());
        }

        return userRepository.save(user);
    }

    public Integer calculateRecommendedCalories(User user) {
        if (user.getWeight() == null || user.getHeight() == null || user.getAge() == null || user.getGender() == null) {
            return null;
        }

        double bmr;
        if (user.getGender() == User.Gender.MALE) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }

        double activityMultiplier = switch (user.getActivityLevel() != null ? user.getActivityLevel() : User.ActivityLevel.SEDENTARY) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTREMELY_ACTIVE -> 1.9;
        };

        return (int) (bmr * activityMultiplier);
    }
}
