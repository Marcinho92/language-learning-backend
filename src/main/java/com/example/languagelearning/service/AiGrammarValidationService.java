package com.example.languagelearning.service;

import com.example.languagelearning.model.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiGrammarValidationService {

    private final ChatClient chatClient;

    public GrammarValidationResult validateSentence(String userSentence, Word word, String grammarTopic) {
        log.info("Validating sentence: '{}' for word: '{}' with grammar topic: '{}'", userSentence, word.getOriginalWord(), grammarTopic);

        try {
            String prompt = buildValidationPrompt(userSentence, word, grammarTopic);

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI response: {}", aiResponse);

            return parseAiResponse(aiResponse, userSentence, word, grammarTopic);

        } catch (Exception e) {
            log.error("Error validating sentence with AI", e);
            return new GrammarValidationResult(false,
                    "Error validating sentence. Please try again.",
                    "AI validation service is temporarily unavailable.");
        }
    }

    private String buildValidationPrompt(String userSentence, Word word, String grammarTopic) {
        return String.format("""
            You are an English grammar teacher. Your task is to validate if a student's sentence is correct according to the given grammar topic and contains the required word.
            
            Student's sentence: \"%s\"
            Required word to use: \"%s\" (translation: \"%s\")
            Grammar topic: \"%s\"
            
            Please analyze the sentence and respond in the following JSON format:
            {
                \"isCorrect\": true/false,
                \"feedback\": \"Brief feedback about the sentence\",
                \"correction\": \"Corrected version of the sentence (if incorrect)\",
                \"explanation\": \"Detailed explanation of the grammar rules applied\"
            }
            
            Rules:
            1. Check if the sentence contains the required word (either original or translation)
            2. Check if the sentence follows the grammar topic rules
            3. Provide helpful feedback for improvement
            4. If incorrect, provide a corrected version
            5. Give a brief explanation of the grammar rules
            
            Grammar topics and their rules:
            - Present Simple: Subject + base verb (add 's' for 3rd person singular)
            - Present Continuous: Subject + be (am/is/are) + verb + ing
            - Past Simple: Subject + past form of verb
            - Past Continuous: Subject + was/were + verb + ing
            - Present Perfect: Subject + have/has + past participle
            - Past Perfect: Subject + had + past participle
            - Future Simple: Subject + will + base verb
            - First Conditional: If + present simple, will + base verb
            - Second Conditional: If + past simple, would + base verb
            - Third Conditional: If + past perfect, would have + past participle
            - Passive Voice: Subject + be + past participle
            - Modal Verbs: Subject + modal verb + base verb
            - Gerunds and Infinitives: verb + ing or to + base verb
            - Relative Clauses: Noun + relative pronoun + clause
            - Reported Speech: Subject + reporting verb + that + reported clause
            
            Respond only with valid JSON.
            """, userSentence, word.getOriginalWord(), word.getTranslation(), grammarTopic);
    }

    private GrammarValidationResult parseAiResponse(String aiResponse, String userSentence, Word word, String grammarTopic) {
        try {
            // Simple JSON parsing - in production you might want to use a proper JSON parser
            boolean isCorrect = aiResponse.toLowerCase().contains("\"iscorrect\":true") ||
                    aiResponse.toLowerCase().contains("\"isCorrect\":true");

            String feedback = extractField(aiResponse, "feedback");
            String correction = extractField(aiResponse, "correction");
            String explanation = extractField(aiResponse, "explanation");

            if (feedback == null) {
                feedback = isCorrect ? "Great job! Your sentence is correct." : "Your sentence needs improvement.";
            }

            if (explanation == null) {
                explanation = generateGrammarExplanation(grammarTopic);
            }

            return new GrammarValidationResult(isCorrect, feedback, explanation);

        } catch (Exception e) {
            log.error("Error parsing AI response: {}", aiResponse, e);
            return new GrammarValidationResult(false,
                    "Error processing AI response. Please try again.",
                    generateGrammarExplanation(grammarTopic));
        }
    }

    private String extractField(String json, String fieldName) {
        try {
            int startIndex = json.indexOf("\"" + fieldName + "\":");
            if (startIndex == -1) return null;

            startIndex = json.indexOf("\"", startIndex + fieldName.length() + 3);
            if (startIndex == -1) return null;

            int endIndex = json.indexOf("\"", startIndex + 1);
            if (endIndex == -1) return null;

            return json.substring(startIndex + 1, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateGrammarExplanation(String grammarTopic) {
        switch (grammarTopic.toLowerCase()) {
            case "present simple":
                return "Present Simple is used for habits, routines, and general truths.\n\n" +
                        "Structure: Subject + base verb (add 's' for 3rd person singular)\n" +
                        "Examples:\n" +
                        "• I work every day.\n" +
                        "• She works in an office.\n" +
                        "• They like coffee.\n" +
                        "• He doesn't like tea.";
            case "present continuous":
                return "Present Continuous is used for actions happening now or around now.\n\n" +
                        "Structure: Subject + be (am/is/are) + verb + ing\n" +
                        "Examples:\n" +
                        "• I am working now.\n" +
                        "• She is reading a book.\n" +
                        "• They are studying English.\n" +
                        "• We are not sleeping.";
            case "past simple":
                return "Past Simple is used for completed actions in the past.\n\n" +
                        "Structure: Subject + past form of verb (regular: +ed, irregular: special form)\n" +
                        "Examples:\n" +
                        "• I worked yesterday.\n" +
                        "• She went to the store.\n" +
                        "• They studied all night.\n" +
                        "• He didn't like the movie.";
            case "past continuous":
                return "Past Continuous is used for actions that were in progress at a specific time in the past.\n\n" +
                        "Structure: Subject + was/were + verb + ing\n" +
                        "Examples:\n" +
                        "• I was working when you called.\n" +
                        "• She was reading a book at 8 PM.\n" +
                        "• They were studying all night.\n" +
                        "• We were not sleeping during the storm.";
            case "present perfect":
                return "Present Perfect is used for actions that started in the past and continue to the present.\n\n" +
                        "Structure: Subject + have/has + past participle\n" +
                        "Examples:\n" +
                        "• I have worked here for 5 years.\n" +
                        "• She has finished her homework.\n" +
                        "• They have never been to Paris.\n" +
                        "• We haven't seen that movie.";
            case "past perfect":
                return "Past Perfect is used for actions that happened before another past action.\n\n" +
                        "Structure: Subject + had + past participle\n" +
                        "Examples:\n" +
                        "• I had finished my work before she arrived.\n" +
                        "• She had already eaten when I called.\n" +
                        "• They had never seen such a beautiful sunset.\n" +
                        "• We hadn't met before the party.";
            case "future simple":
                return "Future Simple is used for predictions and spontaneous decisions.\n\n" +
                        "Structure: Subject + will + base verb\n" +
                        "Examples:\n" +
                        "• I will help you with that.\n" +
                        "• She will be here tomorrow.\n" +
                        "• They will probably come to the party.\n" +
                        "• We won't be late.";
            case "first conditional":
                return "First Conditional is used for real possibilities in the future.\n\n" +
                        "Structure: If + present simple, will + base verb\n" +
                        "Examples:\n" +
                        "• If it rains, I will stay home.\n" +
                        "• If you study hard, you will pass the exam.\n" +
                        "• She will be happy if you call her.\n" +
                        "• We will go to the beach if the weather is nice.";
            case "second conditional":
                return "Second Conditional is used for unreal or hypothetical situations.\n\n" +
                        "Structure: If + past simple, would + base verb\n" +
                        "Examples:\n" +
                        "• If I had money, I would buy a car.\n" +
                        "• If you studied more, you would get better grades.\n" +
                        "• She would travel the world if she could.\n" +
                        "• We would be rich if we won the lottery.";
            case "third conditional":
                return "Third Conditional is used for unreal situations in the past.\n\n" +
                        "Structure: If + past perfect, would have + past participle\n" +
                        "Examples:\n" +
                        "• If I had studied harder, I would have passed the exam.\n" +
                        "• If she had known, she would have told us.\n" +
                        "• They would have won if they had played better.\n" +
                        "• We would have been rich if we had invested earlier.";
            case "passive voice":
                return "Passive Voice is used when the focus is on the action, not the doer.\n\n" +
                        "Structure: Subject + be + past participle (+ by + agent)\n" +
                        "Examples:\n" +
                        "• The book was written by Shakespeare.\n" +
                        "• The house is being built.\n" +
                        "• The letter has been sent.\n" +
                        "• The car was stolen last night.";
            case "modal verbs":
                return "Modal Verbs express ability, possibility, permission, obligation, and advice.\n\n" +
                        "Structure: Subject + modal verb + base verb\n" +
                        "Common modal verbs: can, could, may, might, must, shall, should, will, would\n" +
                        "Examples:\n" +
                        "• I can speak English.\n" +
                        "• You should study harder.\n" +
                        "• She must finish her work.\n" +
                        "• They might come to the party.\n" +
                        "• We could help you with that.";
            case "gerunds and infinitives":
                return "Gerunds and Infinitives are verb forms used as nouns.\n\n" +
                        "Gerund Structure: verb + ing (used as subject, object, after prepositions)\n" +
                        "Infinitive Structure: to + base verb (used after certain verbs, adjectives)\n" +
                        "Examples:\n" +
                        "• I enjoy reading books. (gerund)\n" +
                        "• She wants to learn English. (infinitive)\n" +
                        "• Swimming is good exercise. (gerund as subject)\n" +
                        "• It's important to study regularly. (infinitive)";
            case "relative clauses":
                return "Relative Clauses provide additional information about a noun.\n\n" +
                        "Structure: Noun + relative pronoun (who, which, that, where, when) + clause\n" +
                        "Examples:\n" +
                        "• The man who lives next door is a doctor.\n" +
                        "• The book that I bought is very interesting.\n" +
                        "• The place where I grew up is beautiful.\n" +
                        "• The time when we met was perfect.";
            case "reported speech":
                return "Reported Speech is used to report what someone said.\n\n" +
                        "Structure: Subject + reporting verb + that + reported clause (tense changes)\n" +
                        "Examples:\n" +
                        "• She said that she was tired.\n" +
                        "• He told me that he would come.\n" +
                        "• They mentioned that they had finished.\n" +
                        "• I asked if she could help.";
            default:
                return "Practice using this grammar structure in your sentences.\n\n" +
                        "Make sure to use the given word in your sentence and apply the grammar topic correctly.";
        }
    }

    public static class GrammarValidationResult {
        private final boolean isCorrect;
        private final String feedback;
        private final String explanation;

        public GrammarValidationResult(boolean isCorrect, String feedback, String explanation) {
            this.isCorrect = isCorrect;
            this.feedback = feedback;
            this.explanation = explanation;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public String getFeedback() {
            return feedback;
        }

        public String getExplanation() {
            return explanation;
        }
    }
} 