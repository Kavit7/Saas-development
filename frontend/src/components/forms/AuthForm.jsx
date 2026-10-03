import React, { useState } from "react";
import { configurations } from "../../config/LoginConfig";

const initialValues = {
  login: { email: "", password: "" },
};

const AuthForm = ({ formName, OnSubmit, loading = false }) => {
  const [forms, setForms] = useState(initialValues);
  const config = configurations[formName];

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForms((current) => ({
      ...current,
      [formName]: { ...current[formName], [name]: value },
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;
    await OnSubmit(forms[formName]);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {config.fields.map(([name, label, type]) => (
        <div key={name}>
          <label
            htmlFor={name}
            className="mb-2 block text-sm font-semibold text-[#211917]/80"
          >
            {name === "password" ? "Password" : label}
          </label>
          <input
            id={name}
            name={name}
            type={type}
            value={forms[formName][name]}
            onChange={handleChange}
            placeholder={
              name === "email" ? "you@company.com" : "Enter your password"
            }
            autoComplete={name === "email" ? "username" : "current-password"}
            required
            className="h-12 w-full rounded-xl border border-[#211917]/[0.12] bg-[#f8f8fb] px-4 text-sm text-[#211917] outline-none transition placeholder:text-[#211917]/35 hover:border-[#101B82]/30 focus:border-[#101B82] focus:bg-white focus:ring-4 focus:ring-[#101B82]/[0.08]"
          />
        </div>
      ))}
      <button
        type="submit"
        disabled={loading}
        aria-busy={loading}
        className="group flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-[#101B82] px-4 text-sm font-bold text-white shadow-lg shadow-[#101B82]/20 transition duration-200 hover:-translate-y-0.5 hover:bg-[#0d176f] hover:shadow-xl hover:shadow-[#101B82]/25 focus:outline-none focus:ring-4 focus:ring-[#101B82]/20 active:translate-y-0 disabled:cursor-wait disabled:opacity-80 disabled:hover:translate-y-0"
      >
        {loading ? (
          <>
            <svg
              aria-hidden="true"
              viewBox="0 0 24 24"
              fill="none"
              className="h-5 w-5 animate-spin"
            >
              <circle
                cx="12"
                cy="12"
                r="9"
                stroke="currentColor"
                strokeWidth="3"
                className="opacity-25"
              />
              <path
                d="M21 12a9 9 0 0 0-9-9"
                stroke="currentColor"
                strokeWidth="3"
                strokeLinecap="round"
                className="opacity-90"
              />
            </svg>
            Signing in...
          </>
        ) : (
          <>
            Sign in
            <svg
              aria-hidden="true"
              viewBox="0 0 24 24"
              fill="none"
              className="h-4 w-4 transition-transform group-hover:translate-x-0.5"
            >
              <path
                d="M5 12h14m-6-6 6 6-6 6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </>
        )}
      </button>
    </form>
  );
};

export default AuthForm;
