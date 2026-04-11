import {
  clearAuthState,
  getAccessToken,
  getRefreshToken,
  patchAuthState,
} from "./auth";

const BASE_URL = "http://192.168.110.180:8080";

async function parseBody(response: Response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return { success: false, message: text, errors: [] };
  }
}

function buildHeaders(headers?: HeadersInit, auth?: boolean): Headers {
  const merged = new Headers(headers || {});
  if (!merged.has("Accept")) merged.set("Accept", "application/json");
  if (!merged.has("Content-Type") && !merged.has("X-Skip-Json"))
    merged.set("Content-Type", "application/json");
  merged.delete("X-Skip-Json");
  if (auth) {
    const token = getAccessToken();
    if (token) merged.set("Authorization", `Bearer ${token}`);
  }
  return merged;
}

interface FetchOptions extends RequestInit {
  auth?: boolean;
  retry?: boolean;
}

async function doFetch(path: string, options: FetchOptions = {}): Promise<any> {
  const { auth = true, retry = true, ...requestOptions } = options;
  const url = path.startsWith("http") ? path : `${BASE_URL}${path}`;
  const response = await fetch(url, {
    ...requestOptions,
    headers: buildHeaders(requestOptions.headers as HeadersInit, auth),
  });

  if (response.status === 401 && auth && retry && getRefreshToken()) {
    const refreshed = await refreshTokens();
    if (refreshed) return doFetch(path, { ...options, retry: false });
  }

  const body = await parseBody(response);
  if (!response.ok || body?.success === false) {
    const error: any = new Error(body?.message || `Request failed with status ${response.status}`);
    error.status = response.status;
    error.errors = body?.errors || [];
    error.body = body;
    throw error;
  }
  return body;
}

export async function apiGet(path: string, options: FetchOptions = {}) {
  return doFetch(path, { method: "GET", ...options });
}

export async function apiSend(path: string, method: string, payload?: any, options: FetchOptions = {}) {
  const config: FetchOptions = { method, ...options };
  if (payload !== undefined) {
    config.body = typeof payload === "string" ? payload : JSON.stringify(payload);
  }
  return doFetch(path, config);
}

export async function refreshTokens(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  try {
    const response = await doFetch("/api/v1/refresh", {
      method: "POST",
      auth: false,
      retry: false,
      body: JSON.stringify({ refreshToken }),
    });
    patchAuthState({
      accessToken: response.data.accessToken,
      refreshToken: response.data.refreshToken,
    });
    return true;
  } catch {
    clearAuthState();
    return false;
  }
}

export async function logout() {
  const refreshToken = getRefreshToken();
  try {
    if (refreshToken) await apiSend("/api/v1/logout", "POST", { refreshToken });
  } catch {
    // ignore
  } finally {
    clearAuthState();
  }
}

export async function openAuthenticatedSse(
  path: string,
  { onMessage, onError }: { onMessage?: (data: any) => void; onError?: (err: any) => void }
) {
  const url = path.startsWith("http") ? path : `${BASE_URL}${path}`;
  const response = await fetch(url, {
    headers: {
      Accept: "text/event-stream",
      Authorization: `Bearer ${getAccessToken()}`,
    },
  });
  if (!response.ok || !response.body) {
    const error: any = new Error(`Unable to open stream: ${response.status}`);
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
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split("\n\n");
        buffer = events.pop() || "";
        for (const event of events) {
          const dataLines = event.split("\n").filter((l) => l.startsWith("data:")).map((l) => l.slice(5).trim());
          if (!dataLines.length) continue;
          const payload = dataLines.join("\n");
          try { onMessage?.(JSON.parse(payload)); } catch { onMessage?.(payload); }
        }
      }
    } catch (err) { onError?.(err); }
  };
  pump();
  return { close: () => reader.cancel() };
}
