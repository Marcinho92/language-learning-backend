import React, { useState, useEffect } from 'react';
import {
  TextField,
  Button,
  Typography,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';

const EditWord = ({ word, open, onClose, onSave }) => {
  const [editedWord, setEditedWord] = useState({
    originalWord: '',
    translation: '',
    language: 'english',
    exampleUsage: '',
    explanation: '',
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (word) {
              setEditedWord({
          originalWord: word.originalWord || '',
          translation: word.translation || '',
          language: word.language || 'english',
          exampleUsage: word.exampleUsage || '',
          explanation: word.explanation || '',
        });
    }
  }, [word]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setEditedWord((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'https://language-learning-backend-production.up.railway.app';
      const response = await fetch(`${apiUrl}/api/words/${word.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(word),
      });

      const contentType = response.headers.get('content-type');
      if (response.ok) {
        const updatedWord = await response.json();
        onSave(updatedWord);
        onClose();
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
        setError(errorMessage || `Failed to update word (Status: ${response.status})`);
      }
    } catch (err) {
      console.error('Error details:', {
        message: err.message,
        stack: err.stack
      });
      setError(`An error occurred while updating the word: ${err.message}`);
    }
  };

  const handleClose = () => {
    setError('');
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Edit Word</DialogTitle>
      <DialogContent>
        {error && (
          <Typography color="error" sx={{ mb: 2 }}>
            {error}
          </Typography>
        )}
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
          <TextField
            fullWidth
            margin="normal"
            label="Original Word"
            name="originalWord"
            value={editedWord.originalWord}
            onChange={handleChange}
            required
          />
          <TextField
            fullWidth
            margin="normal"
            label="Translation"
            name="translation"
            value={editedWord.translation}
            onChange={handleChange}
            required
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Language</InputLabel>
            <Select
              name="language"
              value={editedWord.language}
              onChange={handleChange}
              label="Language"
            >
              <MenuItem value="english">English</MenuItem>
              <MenuItem value="polish">Polish</MenuItem>
              <MenuItem value="spanish">Spanish</MenuItem>
              <MenuItem value="german">German</MenuItem>
            </Select>
          </FormControl>

          <TextField
            fullWidth
            margin="normal"
            label="Example Usage (optional)"
            name="exampleUsage"
            value={editedWord.exampleUsage}
            onChange={handleChange}
            multiline
            rows={3}
            placeholder="Enter an example sentence using this word..."
          />
          <TextField
            fullWidth
            margin="normal"
            label="Explanation (optional)"
            name="explanation"
            value={editedWord.explanation}
            onChange={handleChange}
            multiline
            rows={3}
            placeholder="Enter a detailed explanation of the word..."
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          Save Changes
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditWord; 