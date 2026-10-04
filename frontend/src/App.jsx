import "./App.css";
import "./index.css";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useParams,
} from "react-router-dom";
import LoginPage from "./users/LoginPage";
import RequireAuth from "./route/RequireAuth";
import DashboardPage from "./users/DashboardPage";
import ResourcePage from "./users/ResourcePage";
import Layout from "./components/layout/Layout";
import { resources } from "./config/resources";
import { useAuth } from "./hooks/useAuth";

function ResourceRoute() {
  const { resource } = useParams();
  const { hasAccess } = useAuth();
  const config = resources[resource];

  if (!config || !hasAccess(config.roles)) {
    return <Navigate to="/dashboard" replace />;
  }

  if (config.fields) {
    return <ResourcePage key={resource} resourceName={resource} {...config} />;
  }

  if (resource === "dashboard" || config.page === "DashboardPage") {
    return <DashboardPage />;
  }

  if (config.page && typeof config.page !== "string") {
    const Page = config.page;
    return <Page />;
  }
  return <DashboardPage />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route element={<RequireAuth />}>
          <Route element={<Layout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/:resource" element={<ResourceRoute />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
