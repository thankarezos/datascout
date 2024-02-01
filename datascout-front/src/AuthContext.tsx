import React, { createContext, useContext, useState, ReactNode } from 'react';

interface UserData {
  username: string;
  passwd:string;
  // Add more user data fields as needed
}

interface AuthContextType {
  user: UserData | null;
  login: (userData: UserData) => void;
  logout: () => void;
  register: (userData: UserData) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserData | null>(null);

  const login = (userData: UserData) => {
    // call an API to verify credentials
    console.log('karezo psf')

    setUser(userData);
  };

  const logout = () => {
    console.log('karezo psf')
    setUser(null);
  };

  const register :any = (userData: UserData) => {
    console.log('karezo psf')
    //call API
    setUser(userData);
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