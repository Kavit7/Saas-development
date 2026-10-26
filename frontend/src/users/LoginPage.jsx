import { useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";

import AuthForm from "../components/forms/AuthForm";
import DynamicModal from "../components/others/DynamicModal";
import { useAuth } from "../hooks/useAuth";

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, loading, user } = useAuth();
  const [loginError, setLoginError] = useState("");

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

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
    <main className="relative min-h-screen overflow-hidden bg-[#F4F6F3] px-4 py-8 sm:px-6 lg:px-10">
      <DynamicModal
        open={Boolean(loginError)}
        title="Imeshindikana kuingia"
        message={loginError}
        type="error"
        onClose={() => setLoginError("")}
      />
      <div className="relative mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-6xl items-center justify-center">
        <section className="grid w-full overflow-hidden rounded-2xl border border-[#28332C]/10 bg-white shadow-[0_12px_36px_-24px_rgba(40,51,44,0.35)] lg:min-h-[640px] lg:grid-cols-[1.05fr_0.95fr]">
          <div className="relative hidden overflow-hidden bg-[#35634B] px-12 py-12 text-white lg:flex lg:flex-col lg:justify-between xl:px-16">
            <div className="relative flex items-center gap-3">
              <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-xl font-bold text-[#35634B]">
                SS
              </span>
              <div>
                <p className="text-sm font-semibold">Safari Sales</p>
                <p className="mt-0.5 text-xs text-white/60">
                  Operations workspace
                </p>
              </div>
            </div>

            <div className="relative max-w-lg py-12">
              <p className="text-xs font-semibold uppercase tracking-wider text-white/75">
                Your business, in sync
              </p>
              <h2 className="mt-5 text-4xl font-semibold leading-tight tracking-tight xl:text-5xl">
                A simpler way to run your sales operations.
              </h2>
              <p className="mt-5 max-w-md text-base leading-7 text-white/75">
                Safari Sales brings your team and client information together in
                one secure workspace, so you can spend less time switching
                between tools and more time building great customer
                relationships.
              </p>

              <ul className="mt-8 space-y-4 text-sm text-white/90">
                <li className="flex items-center gap-3">
                  <span
                    className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/15 text-white"
                    aria-hidden="true"
                  >
                    ✓
                  </span>
                  Keep client details organized and easy to find
                </li>
                <li className="flex items-center gap-3">
                  <span
                    className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/15 text-white"
                    aria-hidden="true"
                  >
                    ✓
                  </span>
                  Give each team member access to the right tools
                </li>
                <li className="flex items-center gap-3">
                  <span
                    className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/15 text-white"
                    aria-hidden="true"
                  >
                    ✓
                  </span>
                  Manage your sales workflow from one dashboard
                </li>
              </ul>
            </div>
          </div>

          <div className="flex items-center justify-center bg-white px-5 py-8 sm:px-8 lg:px-12">
            <div className="w-full max-w-md">
              <div className="mb-8 text-center lg:text-left">
                <p className="text-sm font-semibold text-[#35634B]">
                  Welcome back
                </p>
                <h1 className="mt-3 text-3xl font-semibold tracking-tight text-[#28332C]">
                  Sign in
                </h1>
                <p className="mt-2 text-sm leading-6 text-[#28332C]/65">
                  Sign in to your Safari Sales workspace to manage clients and
                  keep your sales operations moving.
                </p>
              </div>

              <AuthForm
                formName="login"
                OnSubmit={handleSubmit}
                loading={loading}
              />
            </div>
          </div>
        </section>
      </div>
    </main>
  );
};

export default LoginPage;
