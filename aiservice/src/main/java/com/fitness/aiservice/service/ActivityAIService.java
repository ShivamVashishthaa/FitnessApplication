package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendations(prompt);
        return processAIResponse(activity, aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("'''json\\n", "")
                    .replaceAll("\\n ", "");

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "OverAll");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned");

            List<String> improvements = extreactImprovement(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafety(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(String.valueOf(activity.getType()))
                    .recommendation(String.valueOf(fullAnalysis).trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(String.valueOf(activity.getType()))
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current Routine"))
                .suggestions(Collections.singletonList("Consider Consulting a fitness consultant"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safeties = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(safety -> safeties.add(safety.asText()));
        }
        return safeties.isEmpty()
                ? Collections.singletonList("Follow General Safety Guidelines")
                : safeties;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s : %s", workout, description));
            });
        }
        return suggestions.isEmpty()
                ? Collections.singletonList("No Specific Suggestion needed")
                : suggestions;
    }

    private List<String> extreactImprovement(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String details = improvement.path("recommendation").asText();
                improvements.add(String.format("%s : %s", area, details));
            });
        }
        return improvements.isEmpty()
                ? Collections.singletonList("No Specific improvements provided")
                : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                        Analyze this fitness activity and provide detailed recommendation in the formate in the following Exact JSON format:
                        {
                            "analysis": {
                                "overall": "Overall analysis here",
                                "pace": "Pace analysis here",
                                "heartbeat": "Heart reate analysis here",
                                "caloriesBurned": "Calories analysis here"
                            },
                            "improvements": [
                                {
                                    "area": "Area name",
                                    "recommendation": "Detailed Recommendation"
                                }
                            ],
                            "suggestions": [
                                {
                                    "workout": "Workout_name",
                                    "description": "Detailed workout Description"
                                }
                            ],
                            "safety": [
                                "safety point 1",
                                "safety point 2",
                                "safety point 3",
                                "safety point 4"
                            ]
                        }
                        
                        Analyze this activity:
                        Activity Type: %s
                        Duration: %s minutes
                        Calories Burned: %d
                        Additional Metrics: %s
                        
                        Provide detailed analysis focusing on performance, improvements, next workout suggestions and safety guidelines.
                        Ensure the response follows the EXACT JSON format show above.
                        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()

        );
    }
}
