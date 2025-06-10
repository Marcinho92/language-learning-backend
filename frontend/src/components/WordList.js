import React, { useState, useEffect, useCallback, useMemo } from 'react';
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
  TableSortLabel,
  Button,
  Stack,
} from '@mui/material';
import { Delete, FileUpload, FileDownload } from '@mui/icons-material';

const WordList = () => {
  const [words, setWords] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState('originalWord');
  const [order, setOrder] = useState('asc');

  const fetchWords = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8080/api/words');
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
  }, []);

  useEffect(() => {
    fetchWords();
  }, [fetchWords]);

  const handleRequestSort = useCallback((property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
    setPage(0);
  }, [order, orderBy]);

  const sortedWords = useMemo(() => {
    const comparator = (a, b) => {
      let valueA = a[orderBy];
      let valueB = b[orderBy];
      
      if (order === 'desc') {
        [valueA, valueB] = [valueB, valueA];
      }
      
      if (valueA < valueB) return -1;
      if (valueA > valueB) return 1;
      return 0;
    };

    return [...words].sort(comparator);
  }, [words, order, orderBy]);

  const handleDelete = useCallback(async (id) => {
    try {
      const response = await fetch(`http://localhost:8080/api/words/${id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        fetchWords();
      }
    } catch (error) {
      console.error('Error deleting word:', error);
    }
  }, [fetchWords]);

  const handleChangePage = useCallback((event, newPage) => {
    setPage(newPage);
  }, []);

  const handleChangeRowsPerPage = useCallback((event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }, []);

  const displayedWords = useMemo(() => {
    return sortedWords.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [sortedWords, page, rowsPerPage]);

  const SortableTableCell = React.memo(({ id, label }) => (
    <TableCell>
      <TableSortLabel
        active={true}
        direction={orderBy === id ? order : 'asc'}
        onClick={() => handleRequestSort(id)}
        hideSortIcon={false}
      >
        {label}
      </TableSortLabel>
    </TableCell>
  ));

  const WordTableRow = React.memo(({ word, onDelete }) => (
    <TableRow>
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
          onClick={() => onDelete(word.id)}
        >
          <Delete />
        </IconButton>
      </TableCell>
    </TableRow>
  ));

  const handleExport = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/words/export', {
        method: 'GET',
        headers: {
          'Accept': 'text/csv;charset=utf-8'
        }
      });
      
      if (response.ok) {
        const blob = await response.blob();
        const reader = new FileReader();
        
        reader.onload = () => {
          // Get the text content
          const text = reader.result;
          
          // Create a new blob with UTF-8 encoding
          const utf8Blob = new Blob([text], { 
            type: 'text/csv;charset=utf-8'
          });
          
          // Download the file
          const url = window.URL.createObjectURL(utf8Blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'vocabulary.csv';
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        };
        
        reader.readAsText(blob, 'UTF-8');
      } else {
        console.error('Failed to export words');
      }
    } catch (error) {
      console.error('Error exporting words:', error);
    }
  };

  const handleImport = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      // First read the file as text with UTF-8 encoding
      const text = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject(e);
        reader.readAsText(file, 'UTF-8');
      });

      // Create a new Blob with UTF-8 encoding
      const blob = new Blob([text], { type: 'text/csv;charset=utf-8' });
      const formData = new FormData();
      formData.append('file', blob, file.name);

      const response = await fetch('http://localhost:8080/api/words/import', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        fetchWords(); // Refresh the list after import
      } else {
        const errorText = await response.text();
        console.error('Failed to import words:', errorText);
      }
    } catch (error) {
      console.error('Error importing words:', error);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Your Words
      </Typography>
      
      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <Button
          variant="contained"
          color="primary"
          startIcon={<FileDownload />}
          onClick={handleExport}
        >
          Export to CSV
        </Button>
        <Button
          variant="contained"
          color="primary"
          component="label"
          startIcon={<FileUpload />}
        >
          Import from CSV
          <input
            type="file"
            hidden
            accept=".csv"
            onChange={handleImport}
          />
        </Button>
      </Stack>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <SortableTableCell id="originalWord" label="Original Word" />
              <TableCell>Translation</TableCell>
              <TableCell>Language</TableCell>
              <SortableTableCell id="difficultyLevel" label="Difficulty Level" />
              <SortableTableCell id="proficiencyLevel" label="Proficiency" />
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {displayedWords.map((word) => (
              <WordTableRow 
                key={word.id} 
                word={word} 
                onDelete={handleDelete}
              />
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

export default React.memo(WordList); 