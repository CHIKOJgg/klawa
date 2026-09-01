import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { Bell, Check, Trash2 } from 'lucide-react';

export function NotificationsPage() {
  const [notifs, setNotifs] = useState<any[]>([]);

  useEffect(() => { api.notifications.list().then(setNotifs).catch(() => {}); }, []);

  const markRead = async (id: string) => {
    await api.notifications.markRead(id);
    setNotifs(n => n.map(x => x.id === id ? { ...x, isRead: true } : x));
  };

  const doDelete = async (id: string) => {
    await api.notifications.delete(id);
    setNotifs(n => n.filter(x => x.id !== id));
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-white">Notifications</h1>
      <div className="space-y-2">
        {notifs.map((n: any) => (
          <div key={n.id} className={`flex items-center justify-between bg-gray-900 rounded-xl p-4 border ${n.isRead ? 'border-gray-800' : 'border-indigo-800'}`}>
            <div className="flex items-center gap-3">
              <Bell size={16} className={n.isRead ? 'text-gray-600' : 'text-indigo-400'} />
              <div>
                <p className={`text-sm ${n.isRead ? 'text-gray-400' : 'text-white'}`}>{n.message}</p>
                <p className="text-xs text-gray-600">{n.notificationsType}</p>
              </div>
            </div>
            <div className="flex gap-1">
              {!n.isRead && <button onClick={() => markRead(n.id)} className="p-1.5 text-gray-500 hover:text-green-400 rounded-lg hover:bg-gray-800"><Check size={14} /></button>}
              <button onClick={() => doDelete(n.id)} className="p-1.5 text-gray-500 hover:text-red-400 rounded-lg hover:bg-gray-800"><Trash2 size={14} /></button>
            </div>
          </div>
        ))}
        {notifs.length === 0 && <p className="text-gray-500 text-sm text-center py-8">No notifications.</p>}
      </div>
    </div>
  );
}
