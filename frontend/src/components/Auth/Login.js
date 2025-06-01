import React, { useState } from 'react';
import { TextField, Button, Container, Typography, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const Login = ({ onLogin }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const authHeader = 'Basic ' + btoa(email + ':' + password);
            
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: {
                    'Authorization': authHeader
                }
            });

            if (response.ok) {
                const user = await response.json();
                // Store credentials for later use
                sessionStorage.setItem('authHeader', authHeader);
                sessionStorage.setItem('userEmail', email);
                onLogin(user);
                navigate('/words');
            } else {
                const contentType = response.headers.get('content-type');
                let errorMessage;
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.message;
                } else {
                    const textError = await response.text();
                    errorMessage = textError || `Error: ${response.status} ${response.statusText}`;
                }
                setError(errorMessage || 'Invalid credentials');
            }
        } catch (err) {
            console.error('Login error:', err);
            setError('An error occurred during login');
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography component="h1" variant="h5">
                    Sign in
                </Typography>
                {error && (
                    <Typography color="error" sx={{ mt: 2 }}>
                        {error}
                    </Typography>
                )}
                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Email Address"
                        name="email"
                        autoComplete="email"
                        autoFocus
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                    >
                        Sign In
                    </Button>
                    <Button
                        fullWidth
                        variant="text"
                        onClick={() => navigate('/register')}
                    >
                        Don't have an account? Sign Up
                    </Button>
                </Box>
            </Box>
        </Container>
    );
};

export default Login; 