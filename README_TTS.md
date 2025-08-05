# Text-to-Speech (TTS) Implementation Guide

## Overview
The application now supports text-to-speech functionality for grammar practice responses. Currently, the TTS service is a placeholder that logs the request but returns `null` for audio data.

## Current Implementation

### Features Implemented:
1. **Grammar Practice Response Enhancement**: 
   - Added `audioUrl` field to `GrammarPracticeResponse`
   - Logic to set `correction` field:
     - If `correct = true`: `correction` contains the original user sentence
     - If `correct = false`: `correction` contains the corrected version
   - Audio generation for the `correction` field

2. **New Endpoints**:
   - `POST /api/words/grammar-practice/validate` - Enhanced with audio support
   - `POST /api/words/grammar-practice/audio` - Direct audio generation

3. **TextToSpeechService**: 
   - Placeholder implementation
   - Language mapping for multiple languages
   - Ready for external TTS API integration

## Future TTS Implementation Options

### 1. Google Cloud Text-to-Speech (Recommended)
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-texttospeech</artifactId>
    <version>4.7.0</version>
</dependency>
```

**Pros:**
- High-quality neural voices
- Multiple language support
- Good Spring Boot integration
- Voice selection and customization

**Setup:**
1. Create Google Cloud project
2. Enable Text-to-Speech API
3. Set up service account credentials
4. Add credentials to application.properties

### 2. Amazon Polly
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>polly</artifactId>
    <version>2.20.0</version>
</dependency>
```

**Pros:**
- High-quality voices
- SSML support
- Good AWS integration

### 3. Microsoft Azure Speech Service
```xml
<dependency>
    <groupId>com.microsoft.cognitiveservices.speech</groupId>
    <artifactId>client-sdk</artifactId>
    <version>1.31.0</version>
</dependency>
```

**Pros:**
- Neural voices
- Multiple language support
- Good Azure integration

### 4. OpenAI TTS API
```java
// Using existing Spring AI integration
// Add OpenAI TTS configuration
```

**Pros:**
- Simple integration
- Good quality
- Voice selection

## Implementation Steps for Google Cloud TTS

### 1. Add Dependencies
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-texttospeech</artifactId>
    <version>4.7.0</version>
</dependency>
```

### 2. Configure Credentials
Add to `application.properties`:
```properties
google.cloud.project-id=your-project-id
google.cloud.credentials.location=classpath:service-account-key.json
```

### 3. Update TextToSpeechService
Replace the placeholder implementation with Google Cloud TTS:

```java
@Service
public class TextToSpeechService {
    
    public String generateAudioBase64(String text, String language) {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            
            String languageCode = getLanguageCode(language);
            String voiceName = getVoiceName(language);
            
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(0.9f)
                    .setPitch(0.0f)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            byte[] audioContent = response.getAudioContent().toByteArray();
            return Base64.getEncoder().encodeToString(audioContent);
            
        } catch (IOException e) {
            log.error("Error generating audio", e);
            return null;
        }
    }
}
```

### 4. Voice Mapping
```java
private String getVoiceName(String language) {
    switch (language.toLowerCase()) {
        case "en": return "en-US-Neural2-F";
        case "pl": return "pl-PL-Neural2-A";
        case "es": return "es-ES-Neural2-A";
        case "fr": return "fr-FR-Neural2-A";
        case "de": return "de-DE-Neural2-A";
        default: return "en-US-Neural2-F";
    }
}
```

## API Response Format

### Grammar Practice Validation Response
```json
{
  "word": {
    "id": 1,
    "originalWord": "go",
    "translation": "iść",
    "language": "en"
  },
  "grammarTopic": "present continuous",
  "isCorrect": false,
  "feedback": "Your sentence needs improvement.",
  "correction": "I am going to school",
  "explanation": "Present Continuous is used for actions happening now...",
  "audioUrl": "data:audio/mp3;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUarm7blmGgU7k9n1unEiBC13yO/eizEIHWq+8+OWT..."
}
```

### Direct Audio Generation Response
```json
{
  "audioBase64": "data:audio/mp3;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUarm7blmGgU7k9n1unEiBC13yO/eizEIHWq+8+OWT..."
}
```

## Testing

### Test Grammar Validation with Audio
```bash
curl -X POST "http://localhost:8080/api/words/grammar-practice/validate" \
  -H "Content-Type: application/json" \
  -d '{
    "wordId": 1,
    "userSentence": "I am go to school",
    "grammarTopic": "present continuous"
  }'
```

### Test Direct Audio Generation
```bash
curl -X POST "http://localhost:8080/api/words/grammar-practice/audio" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "I am going to school",
    "language": "en"
  }'
```

## Frontend Integration

### Playing Audio from Base64
```javascript
function playAudio(base64Audio) {
    const audio = new Audio(`data:audio/mp3;base64,${base64Audio}`);
    audio.play();
}

// Example usage
fetch('/api/words/grammar-practice/validate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestData)
})
.then(response => response.json())
.then(data => {
    if (data.audioUrl) {
        playAudio(data.audioUrl);
    }
});
```

## Next Steps

1. **Choose TTS Provider**: Select Google Cloud TTS, Amazon Polly, or Azure Speech
2. **Set up Credentials**: Configure API keys and credentials
3. **Update Dependencies**: Add the chosen TTS library
4. **Implement Audio Generation**: Replace placeholder with real TTS
5. **Test Integration**: Verify audio generation works correctly
6. **Deploy**: Update production environment with TTS support

## Notes

- Audio is returned as Base64-encoded MP3 data
- Language detection is based on the word's language field
- Audio generation is optional and returns null if unavailable
- The service gracefully handles TTS failures 