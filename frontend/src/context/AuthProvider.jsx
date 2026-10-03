import { useMemo, useState } from "react";

import { loginAuth } from "../api/api";
import { jwtDecode } from "jwt-decode";
import { AuthContext } from "./AuthContext";

const AuthProvider = ({ children }) => {
  const [loading, setLoading] = useState(false);
  const [token, setToken] = useState(() => {
    const savedToken = localStorage.getItem("token");
    if (!savedToken) return null;

    try {
      jwtDecode(savedToken);
      return savedToken;
    } catch (error) {
      console.error("Failed to decode stored token:", error);
      localStorage.removeItem("token");
      return null;
    }
  });

  const user = useMemo(() => {
    if (!token) return null;

    try {
      const decode = jwtDecode(token);
      return {
        sub: decode.sub,
        companyId: decode.companyId,
        role_name: decode.role_name,
      };
    } catch (error) {
      console.error("Failed to decode token:", error);
      localStorage.removeItem("token");
      return null;
    }
  }, [token]);

  const login = async (payloads) => {
    setLoading(true);
    try {
      const response = await loginAuth(payloads);

      localStorage.setItem("token", response.token);
      setToken(response.token);
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    localStorage.removeItem("token");
    setToken(null);
  };

  const hasAccess = (allowedRoles) => {
    if (!allowedRoles || allowedRoles.length === 0) return true;
    return allowedRoles.includes(user?.role_name);
  };

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
