import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthForm from "../components/forms/AuthForm";
import DynamicModal from "../components/others/DynamicModal";
import { useAuth } from "../context/AuthProvider";

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
              <span className="mb-5 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/[0.08] px-3 py-1.5 text-[11px] font-semibold uppercase tracking-[0.16em] text-white/80">
                <span className="h-1.5 w-1.5 rounded-full bg-amber-300" />
                Your work, in one place
              </span>
              <h1 className="text-4xl font-bold leading-[1.15] tracking-tight xl:text-[46px]">
                Welcome to your
                <span className="mt-1 block text-amber-200">
                  sales workspace.
                </span>
              </h1>
              <p className="mt-5 max-w-md text-sm leading-7 text-white/70 xl:text-base">
                Manage clients, coordinate your team, and keep every journey
                moving forward from one clear view.
              </p>

              <div className="mt-10 flex items-center gap-3">
                <span className="flex -space-x-2">
                  <span className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#101B82] bg-amber-200 text-[10px] font-bold text-[#101B82]">
                    S
                  </span>
                  <span className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#101B82] bg-sky-200 text-[10px] font-bold text-[#101B82]">
                    O
                  </span>
                  <span className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#101B82] bg-emerald-200 text-[10px] font-bold text-[#101B82]">
                    T
                  </span>
                </span>
                <p className="text-xs text-white/65">
                  Built for teams that make it happen
                </p>
              </div>
            </div>

            <p className="relative text-xs text-white/45">
              Safari Sales · Operations
            </p>
          </div>

          <div className="flex items-center justify-center px-6 py-10 sm:px-12 lg:px-10 xl:px-16">
            <div className="w-full max-w-md">
              <div className="mb-9 lg:hidden">
                <div className="flex items-center gap-3">
                  <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#101B82] text-lg font-black text-white shadow-lg shadow-[#101B82]/20">
                    SS
                  </span>
                  <div>
                    <p className="font-bold text-[#101B82]">Safari Sales</p>
                    <p className="text-xs text-[#211917]/55">
                      Operations workspace
                    </p>
                  </div>
                </div>
              </div>

              <div className="mb-8">
                <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-[#101B82]/65">
                  Welcome back
                </p>
                <h2 className="text-3xl font-bold tracking-tight text-[#211917] sm:text-[34px]">
                  Sign in to continue
                </h2>
                <p className="mt-2 text-sm leading-6 text-[#211917]/60">
                  Enter your work email and password to access your account.
                </p>
              </div>

              <AuthForm
                formName="login"
                OnSubmit={handleSubmit}
                loading={loading}
              />

              <div className="mt-8 flex items-center justify-center gap-2 text-xs text-[#211917]/45">
                <svg
                  aria-hidden="true"
                  viewBox="0 0 24 24"
                  fill="none"
                  className="h-4 w-4 text-[#101B82]/60"
                >
                  <path
                    d="M7 10V7a5 5 0 0 1 10 0v3m-11 0h12a2 2 0 0 1 2 2v8H4v-8a2 2 0 0 1 2-2Z"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                Your sign-in is protected and secure
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
};

export default LoginPage;
