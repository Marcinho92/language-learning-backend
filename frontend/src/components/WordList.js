import React, { useState, useEffect } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  IconButton,
  TablePagination,
  Rating,
  Box,
} from '@mui/material';
import { Delete } from '@mui/icons-material';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

const WordList = () => {
  const [words, setWords] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const navigate = useNavigate();

  useEffect(() => {
    fetchWords();
  }, []);

  const fetchWords = async () => {
    try {
      const authHeader = sessionStorage.getItem('authHeader');
      if (!authHeader) {
        navigate('/login');
        return;
      }

      const response = await fetch('http://localhost:8080/api/words', {
        headers: {
          'Authorization': authHeader
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setWords(data);
      } else {
        const errorText = await response.text();
        console.error('Failed to fetch words:', errorText);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/api/words/${id}`);
      fetchWords();
    } catch (error) {
      console.error('Error deleting word:', error);
    }
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Your Words
      </Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Original Word</TableCell>
              <TableCell>Translation</TableCell>
              <TableCell>Language</TableCell>
              <TableCell>Difficulty Level</TableCell>
              <TableCell>Proficiency</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {words
              .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
              .map((word) => (
                <TableRow key={word.id}>
                  <TableCell>{word.originalWord}</TableCell>
                  <TableCell>{word.translation}</TableCell>
                  <TableCell>{word.language}</TableCell>
                  <TableCell>{word.difficultyLevel}</TableCell>
                  <TableCell>
                    <Rating
                      value={word.proficiencyLevel}
                      readOnly
                      max={5}
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(word.id)}
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={words.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>
    </Box>
  );
};

export default WordList; 