const BASE_URL =
    import.meta.env.VITE_API_URL

const buildUrl = (path, tokenInquery = false, token = null) => {

    const endpoints = `${BASE_URL}${path}`;

    if (!tokenInquery || !token) return endpoints;

    const separator = endpoints.includes("?") ? "&" : "?"
    return `${endpoints}${separator}token =${encodeURIComponent(token)}`;

}

export const apiRequest = async(path, options = {}, token = null) => {
    const { tokenInquery = false, headers = {}, ...requestOptions } = options;

    const response = await fetch(buildUrl(path, tokenInquery, token), {
        ...requestOptions,
        headers: {
            "Content-Type": "application/json",
            ...(token ? {
                Authorization: `Bearer ${token}`
            } : {})
        }
    })

    const data = await response.json().catch(() => {})

    if (!response.ok) {
        throw new Error(data.details || data.message || "Request failed");
    }

    return response.status === 204 ? "Done" :
        data
}

export const loginAuth = (payload) => {
    return apiRequest("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify(payload)
    })
}



// crud

export const getAllData = async(path, token) => {
    return apiRequest(path, {
        method: "GET"
    }, token)
}
export const getDataById = async(path, token) => {
    return apiRequest(path, {
        method: "GET"
    }, token)
}
export const createData = async(path, payload, token) => {
    return apiRequest(path, {
        method: "POST",
        body: JSON.stringify(payload)
    }, token)
}
export const updateData = async(path, payload, token) => {
    return apiRequest(path, {
        method: "PUT",
        body: JSON.stringify(payload)
    }, token)
}

export const deleteData = async(path, id, token) => {
    return apiRequest(path, {
        method: "DELETE"
    }, token)
}

export const changeStatus = async(path, payload, token) => {
    return apiRequest(path, {
        method: "PATCH",
        body: JSON.stringify(payload)
    }, token)
}