import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  Alert,
} from '@mui/material';
import axios from 'axios';

function WordLearning() {
  const [currentWord, setCurrentWord] = useState(null);
  const [translation, setTranslation] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchRandomWord = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/words/random');
      setCurrentWord(response.data);
      setTranslation('');
      setResult(null);
    } catch (error) {
      console.error('Error fetching random word:', error);
    } finally {
      setLoading(false);
    }
  };

  const checkTranslation = async () => {
    try {
      setLoading(true);
      const response = await axios.post('/api/words/check-translation', {
        originalWord: currentWord.originalWord,
        translation: translation,
      });
      setResult(response.data);
    } catch (error) {
      console.error('Error checking translation:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" gutterBottom align="center">
        Learn Words
      </Typography>
      <Paper sx={{ p: 3, mt: 2 }}>
        {!currentWord ? (
          <Box display="flex" justifyContent="center">
            <Button
              variant="contained"
              color="primary"
              onClick={fetchRandomWord}
              disabled={loading}
            >
              Start Learning
            </Button>
          </Box>
        ) : (
          <>
            <Typography variant="h5" gutterBottom>
              Translate this word:
            </Typography>
            <Typography variant="h4" color="primary" gutterBottom>
              {currentWord.originalWord}
            </Typography>
            <Typography variant="subtitle1" gutterBottom>
              Language: {currentWord.language}
            </Typography>
            <Typography variant="subtitle2" gutterBottom>
              Difficulty: {currentWord.difficultyLevel}
            </Typography>
            <Box mt={3}>
              <TextField
                fullWidth
                label="Your translation"
                value={translation}
                onChange={(e) => setTranslation(e.target.value)}
                disabled={loading}
              />
            </Box>
            <Box mt={2} display="flex" gap={2}>
              <Button
                variant="contained"
                color="primary"
                onClick={checkTranslation}
                disabled={!translation || loading}
                fullWidth
              >
                Check
              </Button>
              <Button
                variant="outlined"
                onClick={fetchRandomWord}
                disabled={loading}
                fullWidth
              >
                Next Word
              </Button>
            </Box>
            {result && (
              <Box mt={2}>
                <Alert severity={result.correct ? "success" : "error"}>
                  {result.message}
                  {!result.correct && (
                    <Typography sx={{ mt: 1 }}>
                      Correct translation: {currentWord.translation}
                    </Typography>
                  )}
                </Alert>
              </Box>
            )}
          </>
        )}
      </Paper>
    </Container>
  );
}

export default WordLearning; 