import React, { useState, useEffect, useCallback } from 'react';
import {
  Container,
  Typography,
  TextField,
  Button,
  Box,
  Paper,
  Rating,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';

const WordLearning = () => {
  const [currentWord, setCurrentWord] = useState(null);
  const [translation, setTranslation] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    language: '',
  });

  const fetchRandomWord = useCallback(async () => {
    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'https://language-learning-backend-production.up.railway.app';
      const params = new URLSearchParams();
      if (filters.language) params.append('language', filters.language);

      const response = await fetch(`${apiUrl}/api/words/random?${params}`);

      if (response.ok) {
        const word = await response.json();
        setCurrentWord(word);
        setTranslation('');
        setResult(null);
        setError('');
      } else {
        const errorData = await response.text();
        setError(errorData || 'Failed to fetch word');
        setCurrentWord(null);
      }
    } catch (error) {
      console.error('Error fetching random word:', error);
      setError('Error fetching word. Please try again.');
      setCurrentWord(null);
    }
  }, [filters]);

  useEffect(() => {
    fetchRandomWord();
  }, [fetchRandomWord]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!currentWord) return;

    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'https://language-learning-backend-production.up.railway.app';
      const response = await fetch(
        `${apiUrl}/api/words/${currentWord.id}/check?translation=${encodeURIComponent(translation)}`,
        {
          method: 'POST'
        }
      );

      if (response.ok) {
        const resultData = await response.json();
        setResult({
          correct: resultData.correct,
          message: resultData.message,
          exampleUsage: resultData.exampleUsage,
          explanation: resultData.explanation,
        });
      } else {
        setError('Failed to check translation. Please try again.');
      }
    } catch (error) {
      console.error('Error checking translation:', error);
      setError('Error checking translation. Please try again.');
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" gutterBottom align="center">
        Learn Words
      </Typography>

      <Box sx={{ mb: 4 }}>
        <FormControl sx={{ mr: 2, minWidth: 120 }}>
          <InputLabel>Language</InputLabel>
          <Select
            name="language"
            value={filters.language}
            onChange={handleFilterChange}
            label="Language"
          >
            <MenuItem value="">All</MenuItem>
            <MenuItem value="english">English</MenuItem>
            <MenuItem value="polish">Polish</MenuItem>
            <MenuItem value="spanish">Spanish</MenuItem>
            <MenuItem value="german">German</MenuItem>
          </Select>
        </FormControl>


      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {currentWord ? (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Translate this word:
          </Typography>
          <Typography variant="h4" gutterBottom>
            {currentWord.originalWord}
          </Typography>

          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ mr: 1 }}>
              Current Proficiency:
            </Typography>
            <Rating value={currentWord.proficiencyLevel} max={5} readOnly />
          </Box>
          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Your Translation"
              value={translation}
              onChange={(e) => setTranslation(e.target.value)}
              margin="normal"
              required
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              sx={{ mt: 2 }}
            >
              Check
            </Button>
          </form>
          {result && (
            <Box sx={{ mt: 2 }}>
              <Typography
                color={result.correct ? 'success.main' : 'error.main'}
                variant="h6"
              >
                {result.message}
              </Typography>
              
              {result.exampleUsage && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                    Example Usage:
                  </Typography>
                  <Typography variant="body1" sx={{ fontStyle: 'italic' }}>
                    {result.exampleUsage}
                  </Typography>
                </Box>
              )}
              
              {result.explanation && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                    Explanation:
                  </Typography>
                  <Typography variant="body1">
                    {result.explanation}
                  </Typography>
                </Box>
              )}
              
              <Button
                variant="outlined"
                color="primary"
                fullWidth
                sx={{ mt: 2 }}
                onClick={fetchRandomWord}
              >
                Next Word
              </Button>
            </Box>
          )}
        </Paper>
      ) : !error && (
        <Typography align="center">No words available for the selected criteria.</Typography>
      )}
    </Container>
  );
};

export default WordLearning; 