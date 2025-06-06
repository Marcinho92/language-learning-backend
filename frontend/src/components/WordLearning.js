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
import { useNavigate } from 'react-router-dom';
import { API_URL } from '../config';

const WordLearning = () => {
  const [currentWord, setCurrentWord] = useState(null);
  const [userTranslation, setUserTranslation] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    language: '',
    difficultyLevel: '',
  });
  const navigate = useNavigate();

  const fetchRandomWord = useCallback(async () => {
    try {
      const authHeader = sessionStorage.getItem('authHeader');
      if (!authHeader) {
        navigate('/login');
        return;
      }

      const params = new URLSearchParams({
        ...(filters.language && { language: filters.language }),
        ...(filters.difficultyLevel && { difficultyLevel: filters.difficultyLevel })
      }).toString();

      const response = await fetch(`${API_URL}/api/words/random?${params}`, {
        headers: {
          'Authorization': authHeader
        }
      });

      if (response.ok) {
        const word = await response.json();
        setCurrentWord(word);
        setUserTranslation('');
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
  }, [filters, navigate]);

  useEffect(() => {
    fetchRandomWord();
  }, [fetchRandomWord]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!currentWord) return;

    try {
      const authHeader = sessionStorage.getItem('authHeader');
      if (!authHeader) {
        navigate('/login');
        return;
      }

      const response = await fetch(
        `${API_URL}/api/words/${currentWord.id}/check?translation=${encodeURIComponent(userTranslation)}`,
        {
          method: 'POST',
          headers: {
            'Authorization': authHeader
          }
        }
      );

      if (response.ok) {
        const isCorrect = await response.json();
        setResult({
          correct: isCorrect,
          message: isCorrect ? 'Correct!' : `Incorrect. The correct answer is: ${currentWord.translation}`,
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

        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Difficulty</InputLabel>
          <Select
            name="difficultyLevel"
            value={filters.difficultyLevel}
            onChange={handleFilterChange}
            label="Difficulty"
          >
            <MenuItem value="">All</MenuItem>
            <MenuItem value={1}>Easy</MenuItem>
            <MenuItem value={2}>Medium</MenuItem>
            <MenuItem value={3}>Hard</MenuItem>
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
              Difficulty:
            </Typography>
            <Rating value={currentWord.difficultyLevel} max={3} readOnly />
          </Box>
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
              value={userTranslation}
              onChange={(e) => setUserTranslation(e.target.value)}
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