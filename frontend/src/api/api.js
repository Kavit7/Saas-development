const BASE_URL = import.meta.env.VITE_API_URL;

const buildUrl = (path, tokenInquery = false, token = null) => {
  const endpoints = `${BASE_URL}${path}`;

  if (!tokenInquery || !token) return endpoints;

  const separator = endpoints.includes("?") ? "&" : "?";
  return `${endpoints}${separator}token=${encodeURIComponent(token)}`;
};

export const apiRequest = async (path, options = {}, token = null) => {
  const {
    tokenInquery = false,
    headers: customHeaders = {},
    ...requestOptions
  } = options;

  const response = await fetch(buildUrl(path, tokenInquery, token), {
    ...requestOptions,
    headers: {
      "Content-Type": "application/json",
      ...customHeaders,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (response.status === 204) {
    return "Done";
  }

  const text = await response.text().catch(() => "");
  let data;
  try {
    data = text ? JSON.parse(text) : undefined;
  } catch {
    data = { message: text };
  }

  if (!response.ok) {
    const errorMsg =
      data?.details ||
      data?.message ||
      data?.error ||
      (typeof data === "string" ? data : "") ||
      (typeof text === "string" && text.length < 300 ? text : "") ||
      "Request failed";
    throw new Error(errorMsg);
  }

  return data;
};

export const loginAuth = (payload) => {
  return apiRequest("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
};

// crud

export const getAllData = async (path, token) => {
  return apiRequest(path, {
    method: "GET",
  }, token);
};

export const getDataById = async (path, token) => {
  return apiRequest(path, {
    method: "GET",
  }, token);
};

export const createData = async (path, payload, token) => {
  return apiRequest(
    path,
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
};

export const updateData = async (path, payload, token) => {
  return apiRequest(
    path,
    {
      method: "PUT",
      body: JSON.stringify(payload),
    },
    token,
  );
};

export const deleteData = async (path, idOrToken = null, maybeToken = null) => {
  let targetPath = path;
  let token = idOrToken;

  if (maybeToken !== null) {
    targetPath = `${path}/${idOrToken}`;
    token = maybeToken;
  }

  return apiRequest(
    targetPath,
    {
      method: "DELETE",
    },
    token,
  );
};

export const changeStatus = async (path, payload, token) => {
  return apiRequest(
    path,
    {
      method: "PATCH",
      body: JSON.stringify(payload),
    },
    token,
  );
};
