import { createContext, useEffect, useState } from "react";

import { loginAuth } from "../api/api";
import { jwtDecode } from "jwt-decode";

export const AuthContext = createContext(null);

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState({
    sub: "",
    company_id: "",
    role_name: "",
  });
  const [loading, setLoading] = useState(false);
  const [token, setToken] = useState(() => localStorage.getItem("token"));

  const login = async (payloads) => {
    setLoading(true);
    try {
      const response = await loginAuth(payloads);

      localStorage.setItem("token", response.token);
      setToken(response.token);
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
    setToken(null);
  };

  const hasAccess = (allowedRoles) => {
    if (!allowedRoles || allowedRoles.length === 0) return true;
    return allowedRoles.includes(user?.role_name);
  };

  useEffect(() => {
    if (!token) {
      setUser(null);
      return;
    }

    try {
      const decode = jwtDecode(token);
      setUser({
        sub: decode.sub,
        companyId: decode.companyId,
        role_name: decode.role_name,
      });
    } catch (error) {
      console.error("Failed to decode token:", error);
      setUser(null);
      localStorage.removeItem("token");
      setToken(null);
    }
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

export default AuthProvider;
