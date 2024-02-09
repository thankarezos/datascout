import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
// import UploadPage from './Upload';
import NormalLoginForm from './Login';
import PicturesWall from './Page';
import NormalRegisterForm from './Register';

// import AuthPage from './AuthPage';

const App: React.FC = () => {
  return (
      <Router>
        <Routes>
          <Route path="/login" element={<NormalLoginForm />} />
          <Route path="/register" element={<NormalRegisterForm />} />

          <Route path="/" element={<PicturesWall />} />
          {/* <Route path="/upload" component={UploadPage} /> */}
        </Routes>
      </Router>
  );
};

export default App;