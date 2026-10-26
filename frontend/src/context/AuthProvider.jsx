import { useMemo, useState, useEffect, useCallback } from "react";
import { jwtDecode } from "jwt-decode";
import { loginAuth, getAllData } from "../api/api";
import { AuthContext } from "./AuthContext";

const normalizeRole = (role) => {
  if (!role) return "";
  return String(role).toUpperCase().replace(/^ROLE_/, "").trim();
};

const AuthProvider = ({ children }) => {
  const [loading, setLoading] = useState(false);
  const [userProfile, setUserProfile] = useState(null);
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

  const [isImpersonating, setIsImpersonating] = useState(() => {
    return Boolean(localStorage.getItem("original_admin_token"));
  });

  const refreshProfile = useCallback(async () => {
    if (!token) return;
    try {
      const profile = await getAllData("/api/v1/auth/me", token);
      if (profile && profile.id) {
        setUserProfile(profile);
      }
    } catch (err) {
      console.warn("Could not load /auth/me profile:", err);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      refreshProfile();
    } else {
      setUserProfile(null);
    }
  }, [token, refreshProfile]);

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
        mustChangePassword: userProfile?.mustChangePassword ?? Boolean(decode.mustChangePassword),
        passwordExpire: userProfile?.passwordExpire,
        firstName: userProfile?.firstName,
        lastName: userProfile?.lastName,
        phone: userProfile?.phone,
        companyName: userProfile?.companyName,
        status: userProfile?.status,
      };
    } catch (error) {
      console.error("Failed to decode token:", error);
      localStorage.removeItem("token");
      return null;
    }
  }, [token, userProfile]);

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
    localStorage.removeItem("original_admin_token");
    setIsImpersonating(false);
    setToken(null);
    setUserProfile(null);
  };

  const impersonateUser = (targetToken) => {
    const currentToken = localStorage.getItem("token");
    if (!localStorage.getItem("original_admin_token") && currentToken) {
      localStorage.setItem("original_admin_token", currentToken);
    }
    localStorage.setItem("token", targetToken);
    setIsImpersonating(true);
    setToken(targetToken);
  };

  const stopImpersonation = () => {
    const originalToken = localStorage.getItem("original_admin_token");
    if (originalToken) {
      localStorage.removeItem("original_admin_token");
      localStorage.setItem("token", originalToken);
      setIsImpersonating(false);
      setToken(originalToken);
    }
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
      value={{
        login,
        token,
        user,
        loading,
        logout,
        hasAccess,
        can,
        refreshProfile,
        isImpersonating,
        impersonateUser,
        stopImpersonation,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
