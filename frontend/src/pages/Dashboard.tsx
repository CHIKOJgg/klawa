import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { ListTodo, FolderKanban, Bell, Clock } from 'lucide-react';

export function Dashboard() {
  const [tasks, setTasks] = useState<any[]>([]);
  const [projects, setProjects] = useState<any[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [reminders, setReminders] = useState<any[]>([]);

  useEffect(() => {
    api.tasks.list().then(setTasks).catch(() => {});
    api.projects.list().then(setProjects).catch(() => {});
    api.notifications.list().then(setNotifications).catch(() => {});
    api.reminders.list().then(setReminders).catch(() => {});
  }, []);

  const cards = [
    { icon: ListTodo, label: 'Tasks', value: tasks.length, color: 'bg-blue-500' },
    { icon: FolderKanban, label: 'Projects', value: projects.length, color: 'bg-emerald-500' },
    { icon: Bell, label: 'Notifications', value: notifications.length, color: 'bg-amber-500' },
    { icon: Clock, label: 'Reminders', value: reminders.length, color: 'bg-purple-500' },
  ];

  const recentTasks = tasks.filter(t => t.status !== 'COMPLETED').slice(0, 5);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-white">Dashboard</h1>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(({ icon: Icon, label, value, color }) => (
          <div key={label} className="bg-gray-900 rounded-xl p-4 border border-gray-800">
            <div className={`w-10 h-10 ${color} rounded-lg flex items-center justify-center mb-3`}>
              <Icon size={20} className="text-white" />
            </div>
            <p className="text-2xl font-bold text-white">{value}</p>
            <p className="text-sm text-gray-400">{label}</p>
          </div>
        ))}
      </div>
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
        <h2 className="text-lg font-semibold text-white mb-3">Open Tasks</h2>
        {recentTasks.length === 0 ? (
          <p className="text-gray-500 text-sm">No open tasks</p>
        ) : (
          <div className="space-y-2">
            {recentTasks.map((t: any) => (
              <div key={t.id} className="flex items-center justify-between bg-gray-800 rounded-lg px-3 py-2">
                <span className="text-sm text-white">{t.title}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full ${t.priority === 'URGENT' ? 'bg-red-900 text-red-300' : t.priority === 'HIGH' ? 'bg-orange-900 text-orange-300' : 'bg-gray-700 text-gray-300'}`}>
                  {t.priority}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
