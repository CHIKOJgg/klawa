const BASE = '/api/v1';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = { ...options?.headers as Record<string, string> };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!headers['Content-Type'] && options?.body) headers['Content-Type'] = 'application/json';
  const res = await fetch(`${BASE}${path}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `HTTP ${res.status}`);
  }
  const text = await res.text();
  return text ? JSON.parse(text) as T : undefined as T;
}

export const api = {
  auth: {
    register: (body: { firstName: string; lastName: string; email: string; password: string }) =>
      request<{ token: string }>('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
    login: (body: { email: string; password: string }) =>
      request<{ token: string }>('/auth/authenticate', { method: 'POST', body: JSON.stringify(body) }),
    me: () => request<any>('/auth/me'),
  },
  tasks: {
    list: () => request<any[]>('/tasks'),
    get: (id: number) => request<any>(`/tasks/${id}`),
    create: (body: any) => request<any>('/tasks', { method: 'POST', body: JSON.stringify(body) }),
    update: (id: number, body: any) => request<any>(`/tasks/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id: number) => request<any>(`/tasks/${id}`, { method: 'DELETE' }),
    complete: (id: number) => request<any>(`/tasks/${id}/complete`, { method: 'POST' }),
    reopen: (id: number) => request<any>(`/tasks/${id}/reopen`, { method: 'POST' }),
    archive: (id: number) => request<any>(`/tasks/${id}/archive`, { method: 'POST' }),
  },
  projects: {
    list: () => request<any[]>('/projects'),
    get: (id: number) => request<any>(`/projects/${id}`),
    create: (body: any) => request<any>('/projects', { method: 'POST', body: JSON.stringify(body) }),
    update: (id: number, body: any) => request<any>(`/projects/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id: number) => request<any>(`/projects/${id}`, { method: 'DELETE' }),
    tasks: (id: number) => request<any[]>(`/projects/${id}/tasks`),
  },
  chat: {
    send: (message: string) =>
      request<any>('/agent/chat', { method: 'POST', body: JSON.stringify({ message }) }),
    history: () => request<any[]>('/agent/history'),
  },
  reminders: {
    list: () => request<any[]>('/reminders'),
    create: (body: any) => request<any>('/reminders', { method: 'POST', body: JSON.stringify(body) }),
    update: (id: number, body: any) => request<any>(`/remiders/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id: number) => request<any>(`/remiders/${id}`, { method: 'DELETE' }),
    snooze: (id: number) => request<any>(`/remiders/${id}/snooze`, { method: 'POST' }),
  },
  notifications: {
    list: () => request<any[]>('/notifications'),
    markRead: (id: string) => request<any>(`/notifications/${id}/read`, { method: 'PUT' }),
    delete: (id: string) => request<any>(`/notifications/${id}`, { method: 'DELETE' }),
  },
  memories: {
    list: () => request<any[]>('/memories'),
    create: (body: any) => request<any>('/memories', { method: 'POST', body: JSON.stringify(body) }),
    delete: (id: number) => request<any>(`/memories/${id}`, { method: 'DELETE' }),
    search: (q: string) => request<any[]>(`/memories/search?search=${encodeURIComponent(q)}`),
  },
  system: {
    health: () => request<string>('/system/health'),
  },
};
