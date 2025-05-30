import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Navigation from './components/Navigation';
import WordList from './components/WordList';
import WordLearning from './components/WordLearning';
import AddWord from './components/AddWord';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Navigation />
        <Routes>
          <Route path="/" element={<WordList />} />
          <Route path="/learn" element={<WordLearning />} />
          <Route path="/add" element={<AddWord />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App; 