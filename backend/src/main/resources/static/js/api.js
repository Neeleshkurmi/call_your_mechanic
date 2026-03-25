import {
    clearAuthState,
    getAccessToken,
    getAuthState,
    getRefreshToken,
    patchAuthState
} from "./auth.js";

async function parseBody(response) {
    const text = await response.text();
    if (!text) {
        return null;
    }
    try {
        return JSON.parse(text);
    } catch (error) {
        return { success: false, message: text, errors: [] };
    }
}

function buildHeaders(headers, auth) {
    const merged = new Headers(headers || {});
    if (!merged.has("Accept")) {
        merged.set("Accept", "application/json");
    }
    if (!merged.has("Content-Type") && !merged.has("X-Skip-Json")) {
        merged.set("Content-Type", "application/json");
    }
    merged.delete("X-Skip-Json");
    if (auth) {
        const token = getAccessToken();
        if (token) {
            merged.set("Authorization", `Bearer ${token}`);
        }
    }
    return merged;
}

async function doFetch(path, options = {}) {
    const { auth = true, retry = true, ...requestOptions } = options;
    const response = await fetch(path, {
        ...requestOptions,
        headers: buildHeaders(requestOptions.headers, auth)
    });

    if (response.status === 401 && auth && retry && getRefreshToken()) {
        const refreshed = await refreshTokens();
        if (refreshed) {
            return doFetch(path, { ...options, retry: false });
        }
    }

    const body = await parseBody(response);
    if (!response.ok || body?.success === false) {
        const error = new Error(body?.message || `Request failed with status ${response.status}`);
        error.status = response.status;
        error.errors = body?.errors || [];
        error.body = body;
        throw error;
    }
    return body;
}

export async function apiGet(path, options = {}) {
    return doFetch(path, { method: "GET", ...options });
}

export async function apiSend(path, method, payload, options = {}) {
    const config = { method, ...options };
    if (payload !== undefined) {
        config.body = typeof payload === "string" ? payload : JSON.stringify(payload);
    }
    return doFetch(path, config);
}

export async function refreshTokens() {
    const auth = getAuthState();
    if (!auth?.refreshToken) {
        return false;
    }

    try {
        const response = await doFetch("/api/v1/refresh", {
            method: "POST",
            auth: false,
            retry: false,
            body: JSON.stringify({
                refreshToken: auth.refreshToken
            })
        });

        patchAuthState({
            accessToken: response.data.accessToken,
            refreshToken: response.data.refreshToken
        });
        return true;
    } catch (error) {
        clearAuthState();
        return false;
    }
}

export async function logout() {
    const refreshToken = getRefreshToken();
    try {
        if (refreshToken) {
            await apiSend("/api/v1/logout", "POST", { refreshToken });
        }
    } catch (error) {
        // Ignore logout failures; local cleanup still matters.
    } finally {
        clearAuthState();
    }
}

export async function openAuthenticatedSse(path, { onMessage, onError }) {
    const response = await fetch(path, {
        headers: {
            Accept: "text/event-stream",
            Authorization: `Bearer ${getAccessToken()}`
        }
    });

    if (!response.ok || !response.body) {
        const error = new Error(`Unable to open stream: ${response.status}`);
        error.status = response.status;
        throw error;
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    const pump = async () => {
        try {
            while (true) {
                const { value, done } = await reader.read();
                if (done) {
                    break;
                }
                buffer += decoder.decode(value, { stream: true });
                const events = buffer.split("\n\n");
                buffer = events.pop() || "";
                for (const event of events) {
                    const dataLines = event
                            .split("\n")
                            .filter((line) => line.startsWith("data:"))
                            .map((line) => line.slice(5).trim());
                    if (!dataLines.length) {
                        continue;
                    }
                    const payload = dataLines.join("\n");
                    try {
                        onMessage?.(JSON.parse(payload));
                    } catch (error) {
                        onMessage?.(payload);
                    }
                }
            }
        } catch (error) {
            onError?.(error);
        }
    };

    pump();

    return {
        close: () => reader.cancel()
    };
}
