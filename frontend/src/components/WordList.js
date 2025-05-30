import React, { useState, useEffect } from 'react';
import {
  Container,
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
} from '@mui/material';
import { Delete } from '@mui/icons-material';
import axios from 'axios';

function WordList() {
  const [words, setWords] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    fetchWords();
  }, []);

  const fetchWords = async () => {
    try {
      const response = await axios.get('/api/words');
      setWords(response.data);
    } catch (error) {
      console.error('Error fetching words:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await axios.delete(`/api/words/${id}`);
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
    <Container>
      <Typography variant="h4" gutterBottom>
        Word List
      </Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Original Word</TableCell>
              <TableCell>Translation</TableCell>
              <TableCell>Language</TableCell>
              <TableCell>Difficulty</TableCell>
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
    </Container>
  );
}

export default WordList; 