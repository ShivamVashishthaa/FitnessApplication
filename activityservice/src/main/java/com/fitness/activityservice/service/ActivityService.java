package com.fitness.activityservice.service;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.dto.ActivityRequest;
import com.fitness.activityservice.model.dto.ActivityResponse;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final UserValidationService userValidationService;

    private final KafkaTemplate<String, Activity> kafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topicName;

    public ActivityResponse trackActivity(ActivityRequest activityRequest) {
        if (!userValidationService.validateUser(activityRequest.getUserId())) {
            throw new RuntimeException("Invalid User Id");
        }
        Activity activity = mapActivityRequestToActivity(activityRequest);
        Activity savedActivity = activityRepository.save(activity);
        try {
            kafkaTemplate.send(topicName, savedActivity.getUserId(), savedActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapActivityToActivityResponse(savedActivity);

    }

    private Activity mapActivityRequestToActivity(ActivityRequest activityRequest) {
        return Activity.builder()
                .userId(activityRequest.getUserId())
                .type(activityRequest.getType())
                .duration(activityRequest.getDuration())
                .caloriesBurned(activityRequest.getCaloriesBurned())
                .startTime(activityRequest.getStartTime())
                .additionalMetrics(activityRequest.getAdditionalMetrics())
                .build();
    }

    private ActivityResponse mapActivityToActivityResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType())
                .duration(activity.getDuration())
                .caloriesBurned(activity.getCaloriesBurned())
                .startTime(activity.getStartTime())
                .additionalMetrics(activity.getAdditionalMetrics())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}
