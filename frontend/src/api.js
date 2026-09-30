const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8082';

async function handleResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const errorMsg = data.message || data.error || `HTTP error! status: ${response.status}`;
    throw new Error(errorMsg);
  }
  return data;
}

export const urlApi = {
  async shortenUrl({ longUrl, customAlias, expirationDays = 30 }) {
    const res = await fetch(`${API_BASE_URL}/api/urls`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        longUrl,
        customAlias: customAlias || null,
        expirationDays: parseInt(expirationDays)
      })
    });
    return handleResponse(res);
  },

  async getUrlDetails(shortCode) {
    const res = await fetch(`${API_BASE_URL}/api/urls/${shortCode}`);
    return handleResponse(res);
  },

  async getAnalytics(shortCode) {
    const res = await fetch(`${API_BASE_URL}/api/urls/${shortCode}/analytics`);
    return handleResponse(res);
  },

  async getRecentUrls(page = 0, size = 15) {
    const res = await fetch(`${API_BASE_URL}/api/urls?page=${page}&size=${size}`);
    return handleResponse(res);
  },

  async deleteUrl(shortCode) {
    const res = await fetch(`${API_BASE_URL}/api/urls/${shortCode}`, {
      method: 'DELETE'
    });
    return res.status === 204 ? true : handleResponse(res);
  }
};
