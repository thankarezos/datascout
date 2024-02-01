import React, { createContext, useContext, useState, ReactNode } from 'react';
import axios from 'axios';

interface UserData {
  username: string;
  password: string;
  // TI STON PUTSO NA VALW GTPNM
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
}//ti einai kan auto kai giati xriazete

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserData | null>(null);

  const login = async (credentials: { username: string; password: string }) => {
    try {
      //EIMAI AKSIOS
      const response = await axios.post('/login', credentials);

      // PWS KANW ASSERT GTPNGM
      setUser(response.data);

    } catch (error) {
      //den kserw kan an o aksios petaei exception se 4[..] code alla etsi moupe ena poulaki
      console.error('Login failed:', error);
      window.alert('karezo psf');
      throw new Error('Login failed');
    }
  };

  const register = async (credentials: { username: string; password: string }) => {
    try {
      
      await axios.post('/register', credentials);
      //auto login meta to register (?)
      await login(credentials);

    } catch (error) {
      
      console.error('Registration failed:', error);
      window.alert('karezo psf')
      throw new Error('Registration failed');
    }
  };

  const logout = () => {
    window.alert('karezo psf')
    //kodikas pou mporei na leei sto backend oti ekana logout les k exei simasia
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