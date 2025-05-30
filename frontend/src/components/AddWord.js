import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';
import axios from 'axios';

function AddWord() {
  const [word, setWord] = useState({
    originalWord: '',
    translation: '',
    language: 'english',
    difficultyLevel: 1,
  });
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setWord((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      await axios.post('/api/words', word);
      setStatus({
        type: 'success',
        message: 'Word added successfully!',
      });
      setWord({
        originalWord: '',
        translation: '',
        language: 'english',
        difficultyLevel: 1,
      });
    } catch (error) {
      setStatus({
        type: 'error',
        message: 'Error adding word. Please try again.',
      });
      console.error('Error adding word:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" gutterBottom align="center">
        Add New Word
      </Typography>
      <Paper sx={{ p: 3, mt: 2 }}>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Original Word"
            name="originalWord"
            value={word.originalWord}
            onChange={handleChange}
            required
            margin="normal"
          />
          <TextField
            fullWidth
            label="Translation"
            name="translation"
            value={word.translation}
            onChange={handleChange}
            required
            margin="normal"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Language</InputLabel>
            <Select
              name="language"
              value={word.language}
              onChange={handleChange}
              label="Language"
              required
            >
              <MenuItem value="english">English</MenuItem>
              <MenuItem value="polish">Polish</MenuItem>
              <MenuItem value="french">French</MenuItem>
              <MenuItem value="german">German</MenuItem>
              <MenuItem value="spanish">Spanish</MenuItem>
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel>Difficulty Level</InputLabel>
            <Select
              name="difficultyLevel"
              value={word.difficultyLevel}
              onChange={handleChange}
              label="Difficulty Level"
              required
            >
              <MenuItem value={1}>Easy</MenuItem>
              <MenuItem value={2}>Medium</MenuItem>
              <MenuItem value={3}>Hard</MenuItem>
            </Select>
          </FormControl>
          <Box mt={3}>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              disabled={loading}
            >
              Add Word
            </Button>
          </Box>
        </form>
        {status && (
          <Box mt={2}>
            <Alert severity={status.type}>{status.message}</Alert>
          </Box>
        )}
      </Paper>
    </Container>
  );
}

export default AddWord; 