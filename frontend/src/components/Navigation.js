import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Container, Box } from '@mui/material';

const Navigation = () => {
  return (
    <AppBar position="static" sx={{ mb: 4 }}>
      <Container>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Language Learning
          </Typography>
          <Box>
            <Button
              color="inherit"
              component={RouterLink}
              to="/words"
            >
              Words
            </Button>
            <Button
              color="inherit"
              component={RouterLink}
              to="/learn"
            >
              Learn
            </Button>
            <Button
              color="inherit"
              component={RouterLink}
              to="/add"
            >
              Add Word
            </Button>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Navigation; 