package com.nutrition.tracker.dto;

import com.nutrition.tracker.entity.User;
import lombok.Data;

@Data
public class UserProfileRequest {
    private String fullName;
    private Integer age;
    private Double weight;
    private Double height;
    private User.Gender gender;
    private User.ActivityLevel activityLevel;
    private Integer dailyCalorieGoal;
}
