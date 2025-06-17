import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  AppBar,
  Toolbar,
  Button,
  Paper,
  Grid,
} from '@mui/material';

const Dashboard = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            熱量管理系統
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            登出
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h5" component="h2" gutterBottom>
                歡迎使用熱量管理系統
              </Typography>
              <Typography variant="body1">
                這裡將顯示您的熱量攝取記錄和統計數據。
              </Typography>
            </Paper>
          </Grid>
          {/* 這裡可以添加更多功能區塊 */}
        </Grid>
      </Container>
    </Box>
  );
};

export default Dashboard; 