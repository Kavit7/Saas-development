import { useMemo, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { loginAuth } from "../api/api";
import { AuthContext } from "./AuthContext";

const normalizeRole = (role) => {
  if (!role) return "";
  return String(role).toUpperCase().replace(/^ROLE_/, "").trim();
};

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
        id: decode.id,
        email: decode.sub,
        sub: decode.sub,
        companyId: decode.companyId,
        role: decode.role_name,
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
    if (!user || (!user.role_name && !user.role)) return false;
    if (!allowedRoles || !Array.isArray(allowedRoles) || allowedRoles.length === 0) return false;
    const currentRole = normalizeRole(user.role_name || user.role);
    return allowedRoles.some((r) => normalizeRole(r) === currentRole);
  };

  const can = (action, permissions) => {
    if (!action || !permissions || typeof permissions !== "object") {
      return false;
    }
    const allowedRoles = permissions[action];
    if (!Array.isArray(allowedRoles) || allowedRoles.length === 0) return false;
    return hasAccess(allowedRoles);
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
