export function formatCurrency(value: number | string | undefined): string {
  const numeric = Number(value || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(numeric);
}

export function formatDate(value: string | undefined | null): string {
  if (!value) return "Not available";
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function statusClass(status: string): string {
  if (["COMPLETED"].includes(status)) return "success";
  if (["REQUESTED", "ACCEPTED", "ON_THE_WAY", "STARTED"].includes(status)) return "warning";
  if (["CANCELLED"].includes(status)) return "danger";
  return "";
}

export function estimateEta(status: string): string {
  const values: Record<string, string> = {
    REQUESTED: "Awaiting mechanic confirmation",
    ACCEPTED: "About 10 mins",
    ON_THE_WAY: "Arriving soon",
    STARTED: "Service in progress",
    COMPLETED: "Job completed",
    CANCELLED: "Cancelled",
  };
  return values[status] || "Updating";
}

export async function withGeolocation(): Promise<{ lat: number; lng: number }> {
  if (!navigator.geolocation) throw new Error("Location access is not supported.");
  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ lat: Number(pos.coords.latitude.toFixed(6)), lng: Number(pos.coords.longitude.toFixed(6)) }),
      () => reject(new Error("Please allow location access to continue."))
    );
  });
}

export function formatCoordinates(lat: number, lng: number): string {
  return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
}

export async function reverseGeocodeLocation(
  coords: { lat: number; lng: number },
  apiKey?: string
): Promise<string> {
  if (!apiKey) return formatCoordinates(coords.lat, coords.lng);

  try {
    const params = new URLSearchParams({
      latlng: `${coords.lat},${coords.lng}`,
      key: apiKey,
    });
    const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?${params.toString()}`);
    if (!response.ok) return formatCoordinates(coords.lat, coords.lng);

    const data = await response.json();
    if (data.status !== "OK" || !data.results?.length) return formatCoordinates(coords.lat, coords.lng);

    return data.results[0].formatted_address || formatCoordinates(coords.lat, coords.lng);
  } catch {
    return formatCoordinates(coords.lat, coords.lng);
  }
}

export function readApiError(error: any): string {
  if (error.errors?.length) return error.errors.join(", ");
  return error.message || "Something went wrong.";
}
