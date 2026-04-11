import {
    clearActiveBookingId,
    clearAuthState,
    clearBookingDraft,
    getActiveBookingId,
    getBookingDraft,
    hasAuth,
    patchAuthState,
    resolveHomeRoute,
    saveBookingDraft,
    setActiveBookingId,
    setAuthState
} from "./auth.js";
import { apiGet, apiSend, logout, openAuthenticatedSse } from "./api.js";

const body = document.body;
const page = body.dataset.page;
const roleScope = body.dataset.roleScope;
const bookingIdFromPage = body.dataset.bookingId;
const TRAVEL_RATE_PER_KM = 18;

let mechanicProfileCache = null;

document.addEventListener("DOMContentLoaded", () => {
    setupGlobalUi();
    runPage().catch(handleFatalError);
});

function setupGlobalUi() {
    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.addEventListener("click", async () => {
            await logout();
            window.location.href = "/login";
        });
    });
}

async function runPage() {
    if (page === "login") {
        if (hasAuth()) {
            try {
                const user = await hydrateCurrentUser();
                await routeAuthenticatedUser(user, true);
                return;
            } catch (error) {
                clearAuthState();
            }
        }
        await initLoginPage();
        return;
    }

    if (!hasAuth()) {
        window.location.href = "/login";
        return;
    }

    const user = await hydrateCurrentUser();

    if (page === "profile-setup") {
        if (user.profileCompleted) {
            await routeAuthenticatedUser(user);
            return;
        }
        await initProfileSetupPage(user);
        return;
    }

    if (!user.profileCompleted) {
        window.location.href = "/profile/setup";
        return;
    }

    if (roleScope === "user" && user.role !== "USER") {
        window.location.href = resolveHomeRoute(user.role);
        return;
    }

    if (roleScope === "mechanic" && user.role !== "MECHANIC") {
        window.location.href = "/app/user/home";
        return;
    }

    if (roleScope === "mechanic") {
        const profile = await getMechanicProfile();
        if (page === "mechanic-register" && profile) {
            window.location.href = "/app/mechanic/dashboard";
            return;
        }
        if (page !== "mechanic-register" && !profile) {
            window.location.href = "/app/mechanic/register";
            return;
        }
    }

    const handlers = {
        "user-home": initUserHomePage,
        "user-nearby": initNearbyPage,
        "user-booking": initBookingPage,
        "user-tracking": initTrackingPage,
        "user-rating": initUserRatingPage,
        "user-vehicles": initVehiclesPage,
        "user-profile": initUserProfilePage,
        "mechanic-register": initMechanicRegistrationPage,
        "mechanic-dashboard": initMechanicDashboardPage,
        "mechanic-requests": initMechanicRequestsPage,
        "mechanic-job": initMechanicJobPage,
        "mechanic-earnings": initMechanicEarningsPage,
        "mechanic-profile": initMechanicProfilePage
    };

    const handler = handlers[page];
    if (handler) {
        await handler(user);
    }
}

async function hydrateCurrentUser() {
    const response = await apiGet("/api/v1/users/me");
    patchAuthState({ user: response.data });
    return response.data;
}

async function routeAuthenticatedUser(user, fromLogin = false) {
    if (!user.profileCompleted) {
        window.location.href = "/profile/setup";
        return;
    }

    if (user.role === "MECHANIC") {
        const mechanicProfile = await getMechanicProfile();
        window.location.href = mechanicProfile ? "/app/mechanic/dashboard" : "/app/mechanic/register";
        return;
    }

    window.location.href = fromLogin ? "/profile/setup" : "/app/user/home";
}

async function getMechanicProfile() {
    if (mechanicProfileCache !== null) {
        return mechanicProfileCache;
    }
    try {
        const response = await apiGet("/api/v1/mechanics/me");
        mechanicProfileCache = response.data;
        return mechanicProfileCache;
    } catch (error) {
        if (error.status === 404) {
            mechanicProfileCache = false;
            return null;
        }
        throw error;
    }
}

function showBanner(id, message, variant = "error") {
    const banner = document.getElementById(id);
    if (!banner) {
        showToast(message, variant);
        return;
    }
    banner.className = `banner show ${variant}`;
    banner.textContent = message;
}

function clearBanner(id) {
    const banner = document.getElementById(id);
    if (!banner) {
        return;
    }
    banner.className = "banner";
    banner.textContent = "";
}

function showToast(message, variant = "success") {
    const region = document.getElementById("toast-region");
    if (!region) {
        return;
    }
    const toast = document.createElement("div");
    toast.className = `toast ${variant}`;
    toast.textContent = message;
    region.appendChild(toast);
    window.setTimeout(() => toast.remove(), 3500);
}

function setLoading(element, loading) {
    if (!element) {
        return;
    }
    element.classList.toggle("loading", loading);
    if ("disabled" in element) {
        element.disabled = loading;
    }
}

function readApiError(error) {
    if (error.errors?.length) {
        return error.errors.join(", ");
    }
    return error.message || "Something went wrong.";
}

function formatDate(value) {
    if (!value) {
        return "Not available";
    }
    return new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function formatCurrency(value) {
    const numeric = Number(value || 0);
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(numeric);
}

function calculateTravelEstimate(distanceKm) {
    const distance = Number(distanceKm || 0);
    return Number((distance * TRAVEL_RATE_PER_KM).toFixed(2));
}

function statusClass(status) {
    if (["COMPLETED"].includes(status)) {
        return "success";
    }
    if (["REQUESTED", "ACCEPTED", "ON_THE_WAY", "STARTED"].includes(status)) {
        return "warning";
    }
    if (["CANCELLED"].includes(status)) {
        return "danger";
    }
    return "";
}

async function withGeolocation() {
    if (!navigator.geolocation) {
        throw new Error("Location access is not supported in this browser.");
    }
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
                (position) => {
                    resolve({
                        lat: Number(position.coords.latitude.toFixed(6)),
                        lng: Number(position.coords.longitude.toFixed(6))
                    });
                },
                () => reject(new Error("Please allow location access to continue."))
        );
    });
}

async function initLoginPage() {
    const requestForm = document.getElementById("otp-request-form");
    const verifyForm = document.getElementById("otp-verify-form");
    const mobileInput = document.getElementById("mobile");
    const otpInput = document.getElementById("otp");

    requestForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("auth-banner");
        const submitButton = requestForm.querySelector("button[type='submit']");
        setLoading(submitButton, true);
        try {
            await apiSend("/api/v1/auth/otp/request", "POST", {
                mobile: mobileInput.value.trim()
            }, { auth: false });
            showBanner("auth-banner", "OTP requested. Check the server logs in dev mode for the code.", "success");
            otpInput.focus();
        } catch (error) {
            showBanner("auth-banner", readApiError(error));
        } finally {
            setLoading(submitButton, false);
        }
    });

    verifyForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("auth-banner");
        const submitButton = verifyForm.querySelector("button[type='submit']");
        setLoading(submitButton, true);
        try {
            const response = await apiSend("/api/v1/auth/otp/verify", "POST", {
                mobile: mobileInput.value.trim(),
                otp: otpInput.value.trim()
            }, { auth: false });

            setAuthState({
                accessToken: response.data.accessToken,
                refreshToken: response.data.refreshToken,
                user: {
                    userId: response.data.userId,
                    mobile: response.data.mobile,
                    role: response.data.role,
                    profileCompleted: response.data.profileCompleted
                }
            });

            if (response.data.profileCompleted) {
                await routeAuthenticatedUser(response.data, true);
                return;
            }

            window.location.href = "/profile/setup";
        } catch (error) {
            showBanner("auth-banner", readApiError(error));
        } finally {
            setLoading(submitButton, false);
        }
    });
}

async function initProfileSetupPage(user) {
    const form = document.getElementById("profile-setup-form");
    const nameInput = document.getElementById("full-name");
    const roleInputs = document.querySelectorAll("input[name='role']");
    const mobileEl = document.getElementById("current-mobile");

    mobileEl.textContent = user.mobile || "Unknown";
    nameInput.value = user.name || "";
    roleInputs.forEach((input) => {
        input.checked = input.value === (user.role || "USER");
    });

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("profile-banner");
        const submitButton = form.querySelector("button[type='submit']");
        setLoading(submitButton, true);
        try {
            const selectedRole = form.querySelector("input[name='role']:checked")?.value || "USER";
            const response = await apiSend("/api/v1/users/me/profile", "POST", {
                name: nameInput.value.trim(),
                role: selectedRole
            });

            patchAuthState({
                accessToken: response.data.accessToken,
                refreshToken: response.data.refreshToken,
                user: response.data.user
            });

            if (selectedRole === "MECHANIC") {
                window.location.href = "/app/mechanic/register";
                return;
            }
            window.location.href = "/app/user/home";
        } catch (error) {
            showBanner("profile-banner", readApiError(error));
        } finally {
            setLoading(submitButton, false);
        }
    });
}

async function initUserHomePage() {
    const serviceList = document.getElementById("service-list");
    const vehicleSelect = document.getElementById("vehicle-select");
    const locationLabel = document.getElementById("location-label");
    const setLocationButton = document.getElementById("set-location-button");
    const findButton = document.getElementById("find-mechanic-button");

    const [servicesResponse, vehiclesResponse, latestLocationResponse] = await Promise.all([
        apiGet("/api/v1/services", { auth: false }),
        apiGet("/api/v1/vehicles"),
        apiGet("/api/v1/users/me/location/latest").catch(() => ({ data: null }))
    ]);

    const draft = getBookingDraft() || {};
    let selectedServiceId = draft.serviceId || null;
    let location = draft.location || (latestLocationResponse.data
            ? {
                lat: latestLocationResponse.data.latitude,
                lng: latestLocationResponse.data.longitude
            }
            : null);

    if (location) {
        locationLabel.textContent = `${location.lat}, ${location.lng}`;
        saveBookingDraft({
            ...draft,
            location
        });
    }

    servicesResponse.data.forEach((service) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `service-option${String(selectedServiceId) === String(service.id) ? " selected" : ""}`;
        button.innerHTML = `
            <div class="service-name">${service.name}</div>
            <div class="muted">${service.description}</div>
            <div class="badge-row">
                <span class="pill">${service.vehicleType}</span>
                <span class="pill">${formatCurrency(service.serviceCharge)}</span>
            </div>
        `;
        button.addEventListener("click", () => {
            selectedServiceId = service.id;
            document.querySelectorAll(".service-option").forEach((item) => item.classList.remove("selected"));
            button.classList.add("selected");
        });
        serviceList.appendChild(button);
    });

    vehiclesResponse.data.forEach((vehicle) => {
        const option = document.createElement("option");
        option.value = vehicle.id;
        option.textContent = `${vehicle.brand} ${vehicle.model} (${vehicle.registrationNumber})`;
        vehicleSelect.appendChild(option);
    });
    if (draft.vehicleId) {
        vehicleSelect.value = draft.vehicleId;
    }

    setLocationButton?.addEventListener("click", async () => {
        try {
            location = await withGeolocation();
            locationLabel.textContent = `${location.lat}, ${location.lng}`;
            await apiSend("/api/v1/users/me/location", "POST", {
                latitude: location.lat,
                longitude: location.lng
            });
            saveBookingDraft({
                ...(getBookingDraft() || {}),
                serviceId: selectedServiceId ? Number(selectedServiceId) : undefined,
                vehicleId: vehicleSelect.value ? Number(vehicleSelect.value) : undefined,
                location
            });
            showToast("Current location saved.", "success");
        } catch (error) {
            showBanner("home-banner", readApiError(error));
        }
    });

    findButton?.addEventListener("click", () => {
        clearBanner("home-banner");
        if (!selectedServiceId || !vehicleSelect.value || !location) {
            showBanner("home-banner", "Select a service, choose a vehicle, and set your location first.");
            return;
        }
        saveBookingDraft({
            serviceId: Number(selectedServiceId),
            vehicleId: Number(vehicleSelect.value),
            location
        });
        window.location.href = "/app/user/nearby";
    });
}

async function initNearbyPage() {
    const draft = getBookingDraft();
    const list = document.getElementById("nearby-mechanics");
    const summary = document.getElementById("nearby-summary");
    if (!draft?.location) {
        showBanner("nearby-banner", "Start from the user home screen to choose a service and location.");
        return;
    }

    summary.textContent = `Searching near ${draft.location.lat}, ${draft.location.lng}`;
    const response = await apiGet(`/api/v1/mechanics/nearby?lat=${draft.location.lat}&lng=${draft.location.lng}`);
    if (!response.data.length) {
        list.innerHTML = `<div class="empty-state"><h3>No mechanics nearby</h3><p class="muted">Try refreshing your location or checking again in a few minutes.</p></div>`;
        return;
    }

    response.data.forEach((mechanic) => {
        const card = document.createElement("article");
        card.className = "mechanic-card";
        card.innerHTML = `
            <div class="mechanic-name">${mechanic.name}</div>
            <div class="meta-row">
                <span class="pill">${mechanic.rating ?? "New"} star</span>
                <span class="pill">${mechanic.distanceKm} km away</span>
                <span class="pill">${mechanic.experienceYears} yrs</span>
            </div>
            <p class="muted">${mechanic.skills}</p>
            <button type="button" class="btn btn-primary">Select Mechanic</button>
        `;
        card.querySelector("button").addEventListener("click", () => {
            saveBookingDraft({
                ...draft,
                mechanicId: mechanic.mechanicId,
                mechanicName: mechanic.name,
                mechanicDistanceKm: mechanic.distanceKm
            });
            window.location.href = "/app/user/booking";
        });
        list.appendChild(card);
    });
}

async function initBookingPage() {
    const draft = getBookingDraft();
    if (!draft?.mechanicId) {
        showBanner("booking-banner", "Choose a nearby mechanic first.");
        return;
    }

    const [serviceResponse, vehicleResponse, mechanicResponse] = await Promise.all([
        apiGet(`/api/v1/services/${draft.serviceId}`, { auth: false }),
        apiGet("/api/v1/vehicles"),
        apiGet(`/api/v1/mechanics/${draft.mechanicId}`)
    ]);

    const vehicle = vehicleResponse.data.find((item) => String(item.id) === String(draft.vehicleId));
    document.getElementById("booking-mechanic").textContent = mechanicResponse.data.name;
    document.getElementById("booking-service").textContent = serviceResponse.data.name;
    document.getElementById("booking-vehicle").textContent = vehicle ? `${vehicle.brand} ${vehicle.model}` : "Selected vehicle";
    document.getElementById("booking-location").textContent = `${draft.location.lat}, ${draft.location.lng}`;
    document.getElementById("booking-service-charge").textContent = formatCurrency(serviceResponse.data.serviceCharge);
    document.getElementById("booking-travel-distance").textContent = `${Number(draft.mechanicDistanceKm || 0).toFixed(2)} km`;
    document.getElementById("booking-travel-charge").textContent = formatCurrency(calculateTravelEstimate(draft.mechanicDistanceKm));
    document.getElementById("booking-total-fare").textContent = formatCurrency(
            Number(serviceResponse.data.serviceCharge || 0) + calculateTravelEstimate(draft.mechanicDistanceKm)
    );

    const button = document.getElementById("confirm-booking-button");
    button?.addEventListener("click", async () => {
        clearBanner("booking-banner");
        setLoading(button, true);
        try {
            const response = await apiSend("/api/v1/bookings", "POST", {
                mechanicId: draft.mechanicId,
                vehicleId: draft.vehicleId,
                serviceId: draft.serviceId,
                latitude: draft.location.lat,
                longitude: draft.location.lng
            });
            clearBookingDraft();
            setActiveBookingId(response.data.bookingId);
            window.location.href = `/app/user/tracking/${response.data.bookingId}`;
        } catch (error) {
            showBanner("booking-banner", readApiError(error));
        } finally {
            setLoading(button, false);
        }
    });
}

async function initTrackingPage() {
    let bookingId = bookingIdFromPage;
    if (!bookingId) {
        try {
            const activeBookingsResponse = await apiGet("/api/v1/bookings/active");
            const activeBooking = activeBookingsResponse.data.find((booking) =>
                    ["REQUESTED", "ACCEPTED", "ON_THE_WAY", "STARTED"].includes(booking.status)
            );
            if (activeBooking) {
                bookingId = activeBooking.bookingId;
                setActiveBookingId(bookingId);
            } else {
                bookingId = getActiveBookingId();
            }
        } catch (error) {
            showBanner("tracking-banner", readApiError(error));
            return;
        }
    }
    if (!bookingId) {
        showBanner("tracking-banner", "No active booking selected.");
        return;
    }

    const statusEl = document.getElementById("tracking-status");
    const etaEl = document.getElementById("tracking-eta");
    const mechanicEl = document.getElementById("tracking-mechanic");
    const userCoordinatesEl = document.getElementById("user-coordinates");
    const mechanicCoordinatesEl = document.getElementById("mechanic-coordinates");
    const timelineEl = document.getElementById("tracking-history");
    const cancelButton = document.getElementById("cancel-booking-button");
    const callButton = document.getElementById("call-mechanic-button");
    const serviceChargeEl = document.getElementById("tracking-service-charge");
    const travelChargeEl = document.getElementById("tracking-travel-charge");
    const totalFareEl = document.getElementById("tracking-total-fare");

    const bookingResponse = await apiGet(`/api/v1/bookings/${bookingId}`);
    if (bookingResponse.data.status === "COMPLETED" && !bookingResponse.data.reviewSubmitted) {
        window.location.href = `/app/user/rating/${bookingId}`;
        return;
    }
    const mechanicResponse = await apiGet(`/api/v1/mechanics/${bookingResponse.data.mechanicId}`);
    mechanicEl.textContent = mechanicResponse.data.name;
    statusEl.textContent = bookingResponse.data.status;
    statusEl.className = `status-chip ${statusClass(bookingResponse.data.status)}`;
    etaEl.textContent = estimateEta(bookingResponse.data.status);
    serviceChargeEl.textContent = formatCurrency(bookingResponse.data.serviceCharge);
    travelChargeEl.textContent = formatCurrency(bookingResponse.data.travelCharge);
    totalFareEl.textContent = formatCurrency(bookingResponse.data.totalFare);
    cancelButton.hidden = !["REQUESTED", "ACCEPTED", "ON_THE_WAY", "STARTED"].includes(bookingResponse.data.status);
    if (mechanicResponse.data.mobile) {
        callButton.href = `tel:${mechanicResponse.data.mobile}`;
    }

    const fillLocation = (snapshot) => {
        userCoordinatesEl.textContent = snapshot.userLocation
                ? `${snapshot.userLocation.latitude}, ${snapshot.userLocation.longitude}`
                : "Waiting for user coordinates";
        mechanicCoordinatesEl.textContent = snapshot.mechanicLocation
                ? `${snapshot.mechanicLocation.latitude}, ${snapshot.mechanicLocation.longitude}`
                : "Mechanic location not shared yet";
    };

    const applyLocationEvent = (event) => {
        if (event.actorType === "USER") {
            userCoordinatesEl.textContent = `${event.latitude}, ${event.longitude}`;
        }
        if (event.actorType === "MECHANIC") {
            mechanicCoordinatesEl.textContent = `${event.latitude}, ${event.longitude}`;
        }
    };

    try {
        const snapshotResponse = await apiGet(`/api/v1/bookings/${bookingId}/location/latest`);
        fillLocation(snapshotResponse.data);
    } catch (error) {
        fillLocation({});
    }

    const historyResponse = await apiGet(`/api/v1/bookings/${bookingId}/location/history?limit=5`);
    timelineEl.innerHTML = "";
    [...(historyResponse.data.mechanicLocation || []), ...(historyResponse.data.userLocation || [])]
            .sort((left, right) => new Date(right.serverTimestamp) - new Date(left.serverTimestamp))
            .slice(0, 6)
            .forEach((entry) => {
                const item = document.createElement("div");
                item.className = "timeline-item";
                item.textContent = `${entry.latitude}, ${entry.longitude} at ${formatDate(entry.serverTimestamp)}`;
                timelineEl.appendChild(item);
            });

    cancelButton?.addEventListener("click", async () => {
        try {
            const response = await apiSend(`/api/v1/bookings/${bookingId}/cancel`, "PATCH");
            statusEl.textContent = response.data.status;
            statusEl.className = `status-chip ${statusClass(response.data.status)}`;
            showToast("Booking cancelled.", "success");
            clearActiveBookingId();
        } catch (error) {
            showBanner("tracking-banner", readApiError(error));
        }
    });

    try {
        const stream = await openAuthenticatedSse(`/api/v1/bookings/${bookingId}/location/stream`, {
            onMessage: (event) => {
                const payload = event.data || event;
                if (payload?.actorType) {
                    applyLocationEvent(payload);
                }
            },
            onError: () => showToast("Live updates paused. Reload to reconnect.", "error")
        });
        window.addEventListener("beforeunload", () => stream.close(), { once: true });
    } catch (error) {
        showToast("Live stream unavailable, showing latest snapshot only.", "error");
    }
}

async function initUserRatingPage() {
    if (!bookingIdFromPage) {
        showBanner("rating-banner", "Booking id is required to submit a rating.");
        return;
    }

    const form = document.getElementById("rating-form");
    const scoreField = form.elements.namedItem("rating");
    const reviewField = form.elements.namedItem("review");
    const submitButton = document.getElementById("submit-rating-button");

    const bookingResponse = await apiGet(`/api/v1/bookings/${bookingIdFromPage}`);
    const mechanicResponse = await apiGet(`/api/v1/mechanics/${bookingResponse.data.mechanicId}`);
    const serviceResponse = await apiGet(`/api/v1/services/${bookingResponse.data.serviceId}`, { auth: false });

    document.getElementById("rating-mechanic-name").textContent = mechanicResponse.data.name;
    document.getElementById("rating-booking-fare").textContent = `Final fare ${formatCurrency(bookingResponse.data.totalFare)}`;
    document.getElementById("rating-booking-id").textContent = `Booking #${bookingResponse.data.bookingId}`;
    document.getElementById("rating-service-name").textContent = serviceResponse.data.name;

    if (bookingResponse.data.status !== "COMPLETED") {
        showBanner("rating-banner", "You can rate a mechanic only after the booking is completed.");
        form.style.display = "none";
        return;
    }

    if (bookingResponse.data.reviewSubmitted) {
        try {
            const reviewResponse = await apiGet(`/api/v1/bookings/${bookingIdFromPage}/review`);
            scoreField.value = String(reviewResponse.data.rating);
            reviewField.value = reviewResponse.data.review || "";
        } catch (error) {
            // Keep the already-reviewed state even if review fetch fails.
        }
        showBanner("rating-banner", "You have already rated this mechanic.", "success");
        Array.from(form.elements).forEach((element) => {
            element.disabled = true;
        });
        return;
    }

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("rating-banner");
        setLoading(submitButton, true);
        try {
            await apiSend(`/api/v1/bookings/${bookingIdFromPage}/review`, "POST", {
                rating: Number(scoreField.value),
                review: reviewField.value.trim()
            });
            showToast("Thanks for rating your mechanic.", "success");
            window.location.href = "/app/user/home";
        } catch (error) {
            showBanner("rating-banner", readApiError(error));
        } finally {
            setLoading(submitButton, false);
        }
    });
}

function estimateEta(status) {
    const values = {
        REQUESTED: "Awaiting mechanic confirmation",
        ACCEPTED: "About 10 mins",
        ON_THE_WAY: "Arriving soon",
        STARTED: "Service in progress",
        COMPLETED: "Job completed",
        CANCELLED: "Cancelled"
    };
    return values[status] || "Updating";
}

async function initVehiclesPage() {
    const list = document.getElementById("vehicle-list");
    const form = document.getElementById("vehicle-form");
    const title = document.getElementById("vehicle-form-title");
    const vehicleTypeField = form.elements.namedItem("vehicleType");
    const brandField = form.elements.namedItem("brand");
    const modelField = form.elements.namedItem("model");
    const registrationField = form.elements.namedItem("registrationNumber");
    let editingId = null;

    const renderVehicles = async () => {
        const response = await apiGet("/api/v1/vehicles");
        list.innerHTML = "";
        if (!response.data.length) {
            list.innerHTML = `<div class="empty-state"><h3>No vehicles added yet</h3><p class="muted">Add your first vehicle to speed up roadside bookings.</p></div>`;
            return;
        }
        response.data.forEach((vehicle) => {
            const card = document.createElement("article");
            card.className = "vehicle-option";
            card.innerHTML = `
                <div class="vehicle-name">${vehicle.brand} ${vehicle.model}</div>
                <div class="meta-row">
                    <span class="pill">${vehicle.vehicleType}</span>
                    <span class="pill">${vehicle.registrationNumber}</span>
                </div>
                <div class="button-row">
                    <button type="button" class="btn btn-secondary" data-edit>Edit</button>
                    <button type="button" class="btn btn-danger" data-delete>Delete</button>
                </div>
            `;
            card.querySelector("[data-edit]").addEventListener("click", () => {
                editingId = vehicle.id;
                title.textContent = "Edit vehicle";
                vehicleTypeField.value = vehicle.vehicleType;
                brandField.value = vehicle.brand;
                modelField.value = vehicle.model;
                registrationField.value = vehicle.registrationNumber;
            });
            card.querySelector("[data-delete]").addEventListener("click", async () => {
                await apiSend(`/api/v1/vehicles/${vehicle.id}`, "DELETE");
                showToast("Vehicle deleted.", "success");
                if (editingId === vehicle.id) {
                    form.reset();
                    editingId = null;
                }
                await renderVehicles();
            });
            list.appendChild(card);
        });
    };

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("vehicles-banner");
        const payload = {
            vehicleType: vehicleTypeField.value,
            brand: brandField.value.trim(),
            model: modelField.value.trim(),
            registrationNumber: registrationField.value.trim()
        };
        try {
            if (editingId) {
                await apiSend(`/api/v1/vehicles/${editingId}`, "PUT", payload);
                showToast("Vehicle updated.", "success");
            } else {
                await apiSend("/api/v1/vehicles", "POST", payload);
                showToast("Vehicle added.", "success");
            }
            editingId = null;
            title.textContent = "Add vehicle";
            form.reset();
            await renderVehicles();
        } catch (error) {
            showBanner("vehicles-banner", readApiError(error));
        }
    });

    await renderVehicles();
}

async function initUserProfilePage() {
    const form = document.getElementById("user-profile-form");
    const nameField = form.elements.namedItem("name");
    const mobileField = form.elements.namedItem("mobile");
    const roleField = form.elements.namedItem("role");
    const response = await apiGet("/api/v1/users/me");
    nameField.value = response.data.name || "";
    mobileField.value = response.data.mobile || "";
    roleField.value = response.data.role || "USER";

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("user-profile-banner");
        try {
            const updateResponse = await apiSend("/api/v1/users/me", "PUT", {
                name: nameField.value.trim(),
                role: roleField.value
            });
            patchAuthState({ user: updateResponse.data });
            showBanner("user-profile-banner", "Profile updated successfully.", "success");
        } catch (error) {
            showBanner("user-profile-banner", readApiError(error));
        }
    });
}

async function initMechanicRegistrationPage() {
    const form = document.getElementById("mechanic-register-form");
    const experienceField = form.elements.namedItem("experienceYears");
    const skillsField = form.elements.namedItem("skills");
    const bioField = form.elements.namedItem("bio");
    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("mechanic-register-banner");
        try {
            const response = await apiSend("/api/v1/mechanics/register", "POST", {
                experienceYears: Number(experienceField.value),
                skills: skillsField.value.trim(),
                bio: bioField.value.trim()
            });
            patchAuthState({
                accessToken: response.data.accessToken,
                refreshToken: response.data.refreshToken,
                user: response.data.mechanic
                        ? {
                            userId: response.data.mechanic.userId,
                            name: response.data.mechanic.name,
                            mobile: response.data.mechanic.mobile,
                            role: "MECHANIC"
                        }
                        : { role: "MECHANIC" }
            });
            mechanicProfileCache = null;
            showToast("Mechanic profile created.", "success");
            window.location.href = "/app/mechanic/dashboard";
        } catch (error) {
            showBanner("mechanic-register-banner", readApiError(error));
        }
    });
}

async function initMechanicDashboardPage() {
    const [profile, earningsResponse, activeBookingsResponse] = await Promise.all([
        apiGet("/api/v1/mechanics/me"),
        apiGet("/api/v1/mechanics/me/earnings"),
        apiGet("/api/v1/bookings/active")
    ]);

    const availability = document.getElementById("availability-toggle");
    const locationLabel = document.getElementById("mechanic-location-label");
    const setLocationButton = document.getElementById("set-mechanic-location-button");
    const currentBooking = activeBookingsResponse.data[0];
    document.getElementById("mechanic-name").textContent = profile.data.name;
    document.getElementById("earnings-total").textContent = formatCurrency(earningsResponse.data.totalEarnings);
    document.getElementById("completed-jobs").textContent = String(earningsResponse.data.completedJobs || 0);
    document.getElementById("availability-status").textContent = profile.data.available ? "ONLINE" : "OFFLINE";
    document.getElementById("current-booking-status").textContent = currentBooking ? currentBooking.status : "No active booking";
    availability.checked = Boolean(profile.data.available);

    try {
        const locationResponse = await apiGet("/api/v1/mechanics/me/location/latest");
        if (locationResponse.data) {
            locationLabel.textContent = `${locationResponse.data.latitude}, ${locationResponse.data.longitude}`;
        }
    } catch (error) {
        locationLabel.textContent = "Location not captured yet";
    }

    availability?.addEventListener("change", async () => {
        try {
            const response = await apiSend("/api/v1/mechanics/me/availability", "PATCH", {
                available: availability.checked
            });
            document.getElementById("availability-status").textContent = response.data.available ? "ONLINE" : "OFFLINE";
            showToast("Availability updated.", "success");
        } catch (error) {
            availability.checked = !availability.checked;
            showBanner("mechanic-dashboard-banner", readApiError(error));
        }
    });

    setLocationButton?.addEventListener("click", async () => {
        try {
            const location = await withGeolocation();
            await apiSend("/api/v1/mechanics/me/location", "POST", {
                latitude: location.lat,
                longitude: location.lng
            });
            locationLabel.textContent = `${location.lat}, ${location.lng}`;
            showToast("Mechanic location saved.", "success");
        } catch (error) {
            showBanner("mechanic-dashboard-banner", readApiError(error));
        }
    });
}

async function initMechanicRequestsPage() {
    const list = document.getElementById("mechanic-request-list");
    const response = await apiGet("/api/v1/mechanics/me/bookings");
    const requests = response.data.filter((booking) => booking.status === "REQUESTED");
    if (!requests.length) {
        list.innerHTML = `<div class="empty-state"><h3>No incoming requests</h3><p class="muted">Fresh booking requests will appear here.</p></div>`;
        return;
    }

    requests.forEach((booking) => {
        const card = document.createElement("article");
        card.className = "booking-card";
        card.innerHTML = `
            <div class="booking-title">Booking #${booking.bookingId}</div>
            <div class="meta-row">
                <span class="pill">Service ID ${booking.serviceId}</span>
                <span class="pill">${booking.latitude}, ${booking.longitude}</span>
            </div>
            <div class="button-row">
                <button type="button" class="btn btn-primary" data-accept>Accept</button>
                <button type="button" class="btn btn-danger" data-reject>Reject</button>
            </div>
        `;
        card.querySelector("[data-accept]").addEventListener("click", async () => {
            await apiSend(`/api/v1/bookings/${booking.bookingId}/accept`, "PATCH");
            showToast("Booking accepted.", "success");
            window.location.reload();
        });
        card.querySelector("[data-reject]").addEventListener("click", async () => {
            await apiSend(`/api/v1/bookings/${booking.bookingId}/reject`, "PATCH");
            showToast("Booking rejected.", "success");
            window.location.reload();
        });
        list.appendChild(card);
    });
}

async function initMechanicJobPage() {
    const list = document.getElementById("mechanic-active-job");
    const response = await apiGet("/api/v1/bookings/active");
    const active = response.data.find((booking) => ["ACCEPTED", "ON_THE_WAY", "STARTED"].includes(booking.status));
    if (!active) {
        list.innerHTML = `<div class="empty-state"><h3>No active job</h3><p class="muted">Accept a request to start updating job progress.</p></div>`;
        return;
    }

    const nextAction = {
        ACCEPTED: { label: "Start travel", status: "ON_THE_WAY" },
        ON_THE_WAY: { label: "Start job", status: "STARTED" },
        STARTED: { label: "Complete job", status: "COMPLETED" }
    }[active.status];

    list.innerHTML = `
        <article class="booking-card">
            <div class="booking-title">Booking #${active.bookingId}</div>
            <div class="meta-row">
                <span class="status-chip ${statusClass(active.status)}">${active.status}</span>
                <span class="pill">${active.latitude}, ${active.longitude}</span>
            </div>
            <div class="button-row">
                <button type="button" class="btn btn-primary" id="job-next-action">${nextAction.label}</button>
            </div>
        </article>
    `;

    document.getElementById("job-next-action")?.addEventListener("click", async () => {
        try {
            if (nextAction.status === "COMPLETED") {
                await apiSend(`/api/v1/bookings/${active.bookingId}/complete`, "PATCH");
            } else {
                await apiSend(`/api/v1/bookings/${active.bookingId}/status`, "PATCH", {
                    status: nextAction.status
                });
            }
            showToast("Job updated.", "success");
            window.location.reload();
        } catch (error) {
            showBanner("mechanic-job-banner", readApiError(error));
        }
    });
}

async function initMechanicEarningsPage() {
    const [earningsResponse, bookingsResponse] = await Promise.all([
        apiGet("/api/v1/mechanics/me/earnings"),
        apiGet("/api/v1/mechanics/me/bookings")
    ]);

    document.getElementById("earnings-total-value").textContent = formatCurrency(earningsResponse.data.totalEarnings);
    document.getElementById("earnings-completed-value").textContent = String(earningsResponse.data.completedJobs || 0);
    document.getElementById("earnings-average-value").textContent = earningsResponse.data.completedJobs
            ? formatCurrency(earningsResponse.data.totalEarnings / earningsResponse.data.completedJobs)
            : formatCurrency(0);

    const list = document.getElementById("earnings-booking-list");
    const completed = bookingsResponse.data.filter((booking) => booking.status === "COMPLETED");
    if (!completed.length) {
        list.innerHTML = `<div class="empty-state"><h3>No completed jobs yet</h3><p class="muted">Completed jobs will appear here with their completion times.</p></div>`;
        return;
    }
    completed.forEach((booking) => {
        const item = document.createElement("article");
        item.className = "booking-card";
        item.innerHTML = `
            <div class="booking-title">Booking #${booking.bookingId}</div>
            <div class="meta-row">
                <span class="status-chip success">${booking.status}</span>
                <span class="pill">${formatDate(booking.bookingTime)}</span>
            </div>
        `;
        list.appendChild(item);
    });
}

async function initMechanicProfilePage() {
    const form = document.getElementById("mechanic-profile-form");
    const nameField = form.elements.namedItem("name");
    const mobileField = form.elements.namedItem("mobile");
    const skillsField = form.elements.namedItem("skills");
    const experienceField = form.elements.namedItem("experienceYears");
    const bioField = form.elements.namedItem("bio");
    const ratingField = form.elements.namedItem("rating");
    const response = await apiGet("/api/v1/mechanics/me");
    nameField.value = response.data.name || "";
    mobileField.value = response.data.mobile || "";
    skillsField.value = response.data.skills || "";
    experienceField.value = response.data.experienceYears ?? 0;
    bioField.value = response.data.bio || "";
    ratingField.value = response.data.rating ?? "New";

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearBanner("mechanic-profile-banner");
        try {
            const updateResponse = await apiSend("/api/v1/mechanics/me", "PUT", {
                experienceYears: Number(experienceField.value),
                skills: skillsField.value.trim(),
                bio: bioField.value.trim()
            });
            mechanicProfileCache = updateResponse.data;
            showBanner("mechanic-profile-banner", "Mechanic profile updated.", "success");
        } catch (error) {
            showBanner("mechanic-profile-banner", readApiError(error));
        }
    });
}

function handleFatalError(error) {
    const message = readApiError(error);
    if (error.status === 401) {
        clearAuthState();
        window.location.href = "/login";
        return;
    }
    showToast(message, "error");
    const fallback = document.querySelector("[data-fatal-target]");
    if (fallback) {
        fallback.innerHTML = `<div class="empty-state"><h3>We hit a snag</h3><p class="muted">${message}</p></div>`;
    }
}
