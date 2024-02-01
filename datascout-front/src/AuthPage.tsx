import React, { createContext, useContext, useState, ReactNode } from 'react';
import axios from 'axios';

interface UserData {
  username: string;
  password: string;
  // Add more user data fields as needed
}

interface AuthContextType {
  user: UserData | null;
  login: (credentials: { username: string; password: string }) => Promise<void>;
  logout: () => void;
  register: (credentials: { username: string; password: string }) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserData | null>(null);

  const login = async (credentials: { username: string; password: string }) => {
    try {
      // Replace the following URL with your actual login endpoint
      const response = await axios.post('/login', credentials);

      // Assuming the response contains user data upon successful login
      setUser(response.data);

    } catch (error) {
      // Handle login failure, e.g., show an error message
      console.error('Login failed:', error);
      window.alert('karezo psf');
      throw new Error('Login failed');
    }
  };

  const register = async (credentials: { username: string; password: string }) => {
    try {
      // Replace the following URL with your actual register endpoint
      await axios.post('/register', credentials);

      // After successful registration, redirect to login
      await login(credentials);
    } catch (error) {
      // Handle registration failure, e.g., show an error message
      console.error('Registration failed:', error);
      window.alert('karezo psf')
      throw new Error('Registration failed');
    }
  };

  const logout = () => {
    window.alert('karezo psf')
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};