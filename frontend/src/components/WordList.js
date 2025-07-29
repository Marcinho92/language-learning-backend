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
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Delete, FileUpload, FileDownload, Edit, Search } from '@mui/icons-material';
import EditWord from './EditWord';

const WordList = () => {
  const [words, setWords] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState('originalWord');
  const [order, setOrder] = useState('asc');
  const [editingWord, setEditingWord] = useState(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('originalWord');

  const fetchWords = useCallback(async () => {
    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'https://language-learning-backend-production.up.railway.app';
      const response = await fetch(`${apiUrl}/api/words`);
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
    // First filter by search term
    let filteredWords = words;
    if (searchTerm.trim()) {
      filteredWords = words.filter(word => {
        const fieldValue = word[searchField] || '';
        return fieldValue.toLowerCase().includes(searchTerm.toLowerCase());
      });
    }

    // Then sort the filtered words
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

    return [...filteredWords].sort(comparator);
  }, [words, order, orderBy, searchTerm, searchField]);

  const handleDelete = useCallback(async (id) => {
    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
      const response = await fetch(`${apiUrl}/api/words/${id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        fetchWords();
      }
    } catch (error) {
      console.error('Error deleting word:', error);
    }
  }, [fetchWords]);

  const handleEdit = useCallback((word) => {
    setEditingWord(word);
    setEditDialogOpen(true);
  }, []);

  const handleEditClose = useCallback(() => {
    setEditDialogOpen(false);
    setEditingWord(null);
  }, []);

  const handleEditSave = useCallback((updatedWord) => {
    setWords(prevWords => 
      prevWords.map(word => 
        word.id === updatedWord.id ? updatedWord : word
      )
    );
  }, []);

  const handleChangePage = useCallback((event, newPage) => {
    setPage(newPage);
  }, []);

  const handleChangeRowsPerPage = useCallback((event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }, []);

  const handleSearchChange = useCallback((event) => {
    setSearchTerm(event.target.value);
    setPage(0); // Reset to first page when searching
  }, []);

  const handleSearchFieldChange = useCallback((event) => {
    setSearchField(event.target.value);
    setPage(0); // Reset to first page when changing search field
  }, []);

  const displayedWords = useMemo(() => {
    return sortedWords.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [sortedWords, page, rowsPerPage]);

  // Reset to first page when search results change
  useEffect(() => {
    setPage(0);
  }, [searchTerm, searchField]);

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

  const WordTableRow = React.memo(({ word, onDelete, onEdit }) => (
    <TableRow 
      hover 
      onClick={() => onEdit(word)}
      sx={{ cursor: 'pointer', '&:hover': { backgroundColor: 'action.hover' } }}
    >
      <TableCell>{word.originalWord}</TableCell>
      <TableCell>{word.translation}</TableCell>
      <TableCell>{word.language}</TableCell>
      <TableCell>
        <Rating
          value={word.proficiencyLevel}
          readOnly
          max={5}
        />
      </TableCell>
      <TableCell>
        {word.exampleUsage ? (
          <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {word.exampleUsage}
          </Typography>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No example
          </Typography>
        )}
      </TableCell>
      <TableCell>
        {word.explanation ? (
          <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {word.explanation}
          </Typography>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No explanation
          </Typography>
        )}
      </TableCell>
      <TableCell onClick={(e) => e.stopPropagation()}>
        <IconButton
          color="primary"
          onClick={() => onEdit(word)}
          size="small"
        >
          <Edit />
        </IconButton>
        <IconButton
          color="error"
          onClick={() => onDelete(word.id)}
          size="small"
        >
          <Delete />
        </IconButton>
      </TableCell>
    </TableRow>
  ));

  const handleExport = async () => {
    try {
      const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
      const response = await fetch(`${apiUrl}/api/words/export`, {
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

      const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
      const response = await fetch(`${apiUrl}/api/words/import`, {
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

      {/* Search Section */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            label="Search words"
            value={searchTerm}
            onChange={handleSearchChange}
            placeholder="Enter search term..."
            size="small"
            sx={{ minWidth: 200 }}
            InputProps={{
              startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
            }}
          />
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Search by</InputLabel>
            <Select
              value={searchField}
              onChange={handleSearchFieldChange}
              label="Search by"
            >
              <MenuItem value="originalWord">Original Word</MenuItem>
              <MenuItem value="translation">Translation</MenuItem>
            </Select>
          </FormControl>
          {searchTerm && (
            <Typography variant="body2" color="text.secondary">
              Found {sortedWords.length} result{sortedWords.length !== 1 ? 's' : ''}
            </Typography>
          )}
        </Stack>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <SortableTableCell id="originalWord" label="Original Word" />
              <SortableTableCell id="translation" label="Translation" />
              <SortableTableCell id="language" label="Language" />
              <SortableTableCell id="proficiencyLevel" label="Proficiency" />
              <TableCell>Example Usage</TableCell>
              <TableCell>Explanation</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {displayedWords.map((word) => (
              <WordTableRow 
                key={word.id} 
                word={word} 
                onDelete={handleDelete}
                onEdit={handleEdit}
              />
            ))}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={sortedWords.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>
      
      <EditWord
        word={editingWord}
        open={editDialogOpen}
        onClose={handleEditClose}
        onSave={handleEditSave}
      />
    </Box>
  );
};

export default React.memo(WordList); 