import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container, CssBaseline } from '@mui/material';
import WordList from './components/WordList';
import WordLearning from './components/WordLearning';
import AddWord from './components/AddWord';
import Navigation from './components/Navigation';

function App() {
  return (
    <Router>
      <CssBaseline />
      <Container>
        <Navigation />
        <Routes>
          <Route path="/words" element={<WordList />} />
          <Route path="/learn" element={<WordLearning />} />
          <Route path="/add" element={<AddWord />} />
          <Route path="/" element={<WordList />} />
        </Routes>
      </Container>
    </Router>
  );
}

export default App; 