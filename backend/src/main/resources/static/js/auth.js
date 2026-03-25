const AUTH_STORAGE_KEY = "cym.auth";
const BOOKING_DRAFT_KEY = "cym.bookingDraft";
const ACTIVE_BOOKING_KEY = "cym.activeBookingId";

export function getAuthState() {
    try {
        return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY) || "null");
    } catch (error) {
        return null;
    }
}

export function setAuthState(state) {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state));
}

export function patchAuthState(patch) {
    const current = getAuthState() || {};
    setAuthState({ ...current, ...patch });
}

export function clearAuthState() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    sessionStorage.removeItem(BOOKING_DRAFT_KEY);
    sessionStorage.removeItem(ACTIVE_BOOKING_KEY);
}

export function hasAuth() {
    const auth = getAuthState();
    return Boolean(auth?.accessToken && auth?.refreshToken);
}

export function getAccessToken() {
    return getAuthState()?.accessToken || null;
}

export function getRefreshToken() {
    return getAuthState()?.refreshToken || null;
}

export function saveBookingDraft(draft) {
    sessionStorage.setItem(BOOKING_DRAFT_KEY, JSON.stringify(draft));
}

export function getBookingDraft() {
    try {
        return JSON.parse(sessionStorage.getItem(BOOKING_DRAFT_KEY) || "null");
    } catch (error) {
        return null;
    }
}

export function clearBookingDraft() {
    sessionStorage.removeItem(BOOKING_DRAFT_KEY);
}

export function setActiveBookingId(bookingId) {
    sessionStorage.setItem(ACTIVE_BOOKING_KEY, String(bookingId));
}

export function getActiveBookingId() {
    return sessionStorage.getItem(ACTIVE_BOOKING_KEY);
}

export function clearActiveBookingId() {
    sessionStorage.removeItem(ACTIVE_BOOKING_KEY);
}

export function resolveHomeRoute(role) {
    return role === "MECHANIC" ? "/app/mechanic/dashboard" : "/app/user/home";
}
