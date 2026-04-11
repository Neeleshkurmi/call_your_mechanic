const AUTH_STORAGE_KEY = "cym.auth";
const BOOKING_DRAFT_KEY = "cym.bookingDraft";
const ACTIVE_BOOKING_KEY = "cym.activeBookingId";

export interface AuthState {
  accessToken: string;
  refreshToken: string;
  user?: {
    userId: number;
    mobile: string;
    name?: string;
    role: string;
    profileCompleted: boolean;
  };
}

export interface BookingDraft {
  serviceId?: number;
  vehicleId?: number;
  location?: { lat: number; lng: number };
  locationName?: string;
  mechanicId?: number;
  mechanicName?: string;
}

export function getAuthState(): AuthState | null {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY) || "null");
  } catch {
    return null;
  }
}

export function setAuthState(state: AuthState) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state));
}

export function patchAuthState(patch: Partial<AuthState>) {
  const current = getAuthState() || {};
  setAuthState({ ...current, ...patch } as AuthState);
}

export function clearAuthState() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  sessionStorage.removeItem(BOOKING_DRAFT_KEY);
  sessionStorage.removeItem(ACTIVE_BOOKING_KEY);
}

export function hasAuth(): boolean {
  const auth = getAuthState();
  return Boolean(auth?.accessToken && auth?.refreshToken);
}

export function getAccessToken(): string | null {
  return getAuthState()?.accessToken || null;
}

export function getRefreshToken(): string | null {
  return getAuthState()?.refreshToken || null;
}

export function saveBookingDraft(draft: BookingDraft) {
  sessionStorage.setItem(BOOKING_DRAFT_KEY, JSON.stringify(draft));
}

export function getBookingDraft(): BookingDraft | null {
  try {
    return JSON.parse(sessionStorage.getItem(BOOKING_DRAFT_KEY) || "null");
  } catch {
    return null;
  }
}

export function clearBookingDraft() {
  sessionStorage.removeItem(BOOKING_DRAFT_KEY);
}

export function setActiveBookingId(bookingId: number | string) {
  sessionStorage.setItem(ACTIVE_BOOKING_KEY, String(bookingId));
}

export function getActiveBookingId(): string | null {
  return sessionStorage.getItem(ACTIVE_BOOKING_KEY);
}

export function clearActiveBookingId() {
  sessionStorage.removeItem(ACTIVE_BOOKING_KEY);
}

export function resolveHomeRoute(role: string): string {
  return role === "MECHANIC" ? "/app/mechanic/dashboard" : "/app/user/home";
}
