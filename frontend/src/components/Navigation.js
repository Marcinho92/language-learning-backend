import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Container } from '@mui/material';
import { School, List, Add } from '@mui/icons-material';

function Navigation() {
  return (
    <AppBar position="static" sx={{ mb: 4 }}>
      <Container>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Language Learning
          </Typography>
          <Button
            color="inherit"
            component={RouterLink}
            to="/"
            startIcon={<List />}
          >
            Word List
          </Button>
          <Button
            color="inherit"
            component={RouterLink}
            to="/learn"
            startIcon={<School />}
          >
            Learn
          </Button>
          <Button
            color="inherit"
            component={RouterLink}
            to="/add"
            startIcon={<Add />}
          >
            Add Word
          </Button>
        </Toolbar>
      </Container>
    </AppBar>
  );
}

export default Navigation; 