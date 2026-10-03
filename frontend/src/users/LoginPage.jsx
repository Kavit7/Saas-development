import { useState } from "react";
import { useNavigate } from "react-router-dom";

import AuthForm from "../components/forms/AuthForm";
import DynamicModal from "../components/others/DynamicModal";
import { useAuth } from "../hooks/useAuth";

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, loading } = useAuth();
  const [loginError, setLoginError] = useState("");

  const handleSubmit = async (credentials) => {
    try {
      setLoginError("");
      await login(credentials);
      navigate("/dashboard");
    } catch (error) {
      setLoginError(
        error?.message ||
          "Imeshindikana kuingia. Hakiki email na password yako kisha ujaribu tena.",
      );
    }
  };

  return (
    <main className="relative min-h-screen overflow-hidden bg-[#f7f7fb] px-4 py-8 sm:px-6 lg:px-10">
      <DynamicModal
        open={Boolean(loginError)}
        title="Imeshindikana kuingia"
        message={loginError}
        type="error"
        onClose={() => setLoginError("")}
      />
      <div className="pointer-events-none absolute -left-32 -top-36 h-96 w-96 rounded-full bg-[#101B82]/[0.06] blur-3xl" />
      <div className="pointer-events-none absolute -bottom-40 -right-24 h-[28rem] w-[28rem] rounded-full bg-amber-400/[0.12] blur-3xl" />

      <div className="relative mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-6xl items-center justify-center">
        <section className="grid w-full overflow-hidden rounded-[28px] border border-[#211917]/[0.08] bg-white shadow-[0_28px_90px_-42px_rgba(16,27,130,0.35)] lg:min-h-[640px] lg:grid-cols-[1.05fr_0.95fr]">
          <div className="relative hidden overflow-hidden bg-[#101B82] px-12 py-12 text-white lg:flex lg:flex-col lg:justify-between xl:px-16">
            <div className="absolute -right-28 -top-24 h-80 w-80 rounded-full border border-white/10" />
            <div className="absolute -right-12 -top-8 h-48 w-48 rounded-full border border-white/10" />
            <div className="absolute -bottom-40 -left-24 h-96 w-96 rounded-full bg-white/[0.05]" />
            <div className="absolute bottom-24 right-12 h-3 w-3 rounded-full bg-amber-300 shadow-[0_0_28px_8px_rgba(252,211,77,0.25)]" />

            <div className="relative flex items-center gap-3">
              <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-xl font-black tracking-tight text-[#101B82] shadow-lg shadow-black/10">
                SS
              </span>
              <div>
                <p className="text-sm font-bold tracking-wide">Safari Sales</p>
                <p className="mt-0.5 text-xs text-white/60">
                  Operations workspace
                </p>
              </div>
            </div>

            <div className="relative max-w-lg py-12">
            </div>
          </div>

          <div className="flex items-center justify-center bg-white px-5 py-8 sm:px-8 lg:px-12">
            <div className="w-full max-w-md">
              <div className="mb-8 text-center lg:text-left">
                <p className="text-sm font-semibold uppercase tracking-[0.22em] text-[#101B82]">
                  Welcome back
                </p>
                <h1 className="mt-3 text-3xl font-black tracking-tight text-[#211917]">
                  Sign in
                </h1>
              </div>

              <AuthForm formName="login" OnSubmit={handleSubmit} loading={loading} />
            </div>
          </div>
        </section>
      </div>
    </main>
  );
};

export default LoginPage;
