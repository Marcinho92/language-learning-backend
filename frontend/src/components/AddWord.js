import React, { useState } from 'react';
import {
  TextField,
  Button,
  Container,
  Typography,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';

const AddWord = () => {
  const navigate = useNavigate();
  const [word, setWord] = useState({
    originalWord: '',
    translation: '',
    language: 'english',
    difficultyLevel: 1,
  });
  const [error, setError] = useState('');

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
      console.log('Sending word data:', word);
      
      const response = await fetch('http://localhost:8080/api/words', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(word),
      });

      const contentType = response.headers.get('content-type');
      if (response.ok) {
        navigate('/words');
      } else {
        let errorMessage;
        if (contentType && contentType.includes('application/json')) {
          const errorData = await response.json();
          errorMessage = errorData.message;
        } else {
          const textError = await response.text();
          errorMessage = textError || `Error: ${response.status} ${response.statusText}`;
        }
        console.error('Server response:', {
          status: response.status,
          statusText: response.statusText,
          error: errorMessage
        });
        setError(errorMessage || `Failed to add word (Status: ${response.status})`);
      }
    } catch (err) {
      console.error('Error details:', {
        message: err.message,
        stack: err.stack
      });
      setError(`An error occurred while adding the word: ${err.message}`);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 4 }}>
        <Typography variant="h4" gutterBottom>
          Add New Word
        </Typography>
        {error && (
          <Typography color="error" sx={{ mb: 2 }}>
            {error}
          </Typography>
        )}
        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            fullWidth
            margin="normal"
            label="Original Word"
            name="originalWord"
            value={word.originalWord}
            onChange={handleChange}
            required
          />
          <TextField
            fullWidth
            margin="normal"
            label="Translation"
            name="translation"
            value={word.translation}
            onChange={handleChange}
            required
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Language</InputLabel>
            <Select
              name="language"
              value={word.language}
              onChange={handleChange}
              label="Language"
            >
              <MenuItem value="english">English</MenuItem>
              <MenuItem value="polish">Polish</MenuItem>
              <MenuItem value="spanish">Spanish</MenuItem>
              <MenuItem value="german">German</MenuItem>
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel>Difficulty Level</InputLabel>
            <Select
              name="difficultyLevel"
              value={word.difficultyLevel}
              onChange={handleChange}
              label="Difficulty Level"
            >
              <MenuItem value={1}>Easy</MenuItem>
              <MenuItem value={2}>Medium</MenuItem>
              <MenuItem value={3}>Hard</MenuItem>
            </Select>
          </FormControl>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            fullWidth
            sx={{ mt: 3 }}
          >
            Add Word
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default AddWord; 