import React from 'react';
import { BrowserRouter as Router, Route, Switch, Redirect } from 'react-router-dom';
import './App.css';
import UploadPage from './Upload';
import AuthPage from './AuthPage';
import { AuthProvider, useAuth } from './AuthContext';

const PrivateRoute: React.FC = ({ children }) => {
  const { user } = useAuth();

  return user ? <>{children}</> : <Redirect to="/login" />;
};

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <Switch>
          <Route path="/auth" component={AuthPage} />
          <PrivateRoute>
            <Route path="/upload" component={UploadPage} />
          </PrivateRoute>
          <Redirect from="/" to="/upload" />
        </Switch>
      </Router>
    </AuthProvider>
  );
};

export default App;