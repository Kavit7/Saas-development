import React, { useState, useEffect } from "react";
import { useContext, createContext } from "react";

import { loginAuth } from "../api/api";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext(null);

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState({
    sub: "",
    company_id: "",
    role_name: "",
  });
  const [loading, setLoading] = useState(false);
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [data, setData] = useState(null);

  const login = async (payloads) => {
    setLoading(true);
    try {
      const data = await loginAuth(payloads);

      setData(data);
      console.log(data);

      localStorage.setItem("token", data.token);

      setToken(data.token);
    } catch (error) {
      setUser(null);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setUser(null);
    localStorage.removeItem("token");
  };

  const hasAccess = (allowedRoles) => {
    if (!allowedRoles || allowedRoles.length === 0) return true;
    return allowedRoles.includes(user?.role_name);
  };

  console.log(user);
  useEffect(() => {
    if (!token) return;

    const decode = jwtDecode(token);
    setUser({
      sub: decode.sub,
      companyId: decode.companyId,
      role_name: decode.role_name,
    });
  }, [token]);

  const can = (action, permissions) => {
    if (!action || !permissions || typeof permissions !== "object") {
      return false;
    }

    const allowedRoles = permissions[action];
    if (!Array.isArray(allowedRoles)) return false;

    return allowedRoles.includes(user?.role_name);
  };

  return (
    <AuthContext.Provider
      value={{ login, token, user, loading, logout, hasAccess, can }}
    >
      {children}
    </AuthContext.Provider>
  );
};
export const useAuth = () => useContext(AuthContext);

export default AuthProvider;
