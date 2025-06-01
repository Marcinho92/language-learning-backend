import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container, CssBaseline } from '@mui/material';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import WordList from './components/WordList';
import WordLearning from './components/WordLearning';
import AddWord from './components/AddWord';
import Navigation from './components/Navigation';

function App() {
  const [user, setUser] = useState(null);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    setUser(null);
  };

  return (
    <Router>
      <CssBaseline />
      <Container>
        {user && <Navigation onLogout={handleLogout} />}
        <Routes>
          <Route
            path="/login"
            element={user ? <Navigate to="/words" /> : <Login onLogin={handleLogin} />}
          />
          <Route
            path="/register"
            element={user ? <Navigate to="/words" /> : <Register />}
          />
          <Route
            path="/words"
            element={user ? <WordList /> : <Navigate to="/login" />}
          />
          <Route
            path="/learn"
            element={user ? <WordLearning /> : <Navigate to="/login" />}
          />
          <Route
            path="/add"
            element={user ? <AddWord /> : <Navigate to="/login" />}
          />
          <Route
            path="/"
            element={<Navigate to={user ? "/words" : "/login"} />}
          />
        </Routes>
      </Container>
    </Router>
  );
}

export default App; 