import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { Plus, Trash2, Bell } from 'lucide-react';

export function RemindersPage() {
  const [reminders, setReminders] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ message: '', triggerTime: '' });

  useEffect(() => { api.reminders.list().then(setReminders).catch(() => {}); }, []);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.reminders.create({ ...form, triggerTime: new Date(form.triggerTime).toISOString() });
    setShowForm(false); setForm({ message: '', triggerTime: '' });
    api.reminders.list().then(setReminders);
  };

  const doDelete = async (id: number) => { await api.reminders.delete(id); setReminders(r => r.filter(x => x.id !== id)); };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Reminders</h1>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-lg">
          <Plus size={16} /> New Reminder
        </button>
      </div>
      {showForm && (
        <form onSubmit={create} className="bg-gray-900 rounded-xl p-4 border border-gray-800 space-y-3">
          <input value={form.message} onChange={e => setForm(f => ({...f, message: e.target.value}))} placeholder="Reminder message" required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <input type="datetime-local" value={form.triggerTime} onChange={e => setForm(f => ({...f, triggerTime: e.target.value}))} required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <div className="flex gap-2">
            <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="text-gray-400 hover:text-white px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
      <div className="space-y-2">
        {reminders.map((r: any) => (
          <div key={r.id} className="flex items-center justify-between bg-gray-900 rounded-xl p-4 border border-gray-800">
            <div className="flex items-center gap-3">
              <Bell size={16} className="text-indigo-400" />
              <div>
                <p className="text-sm text-white">{r.message}</p>
                <p className="text-xs text-gray-500">{new Date(r.triggerTime).toLocaleString()}</p>
              </div>
            </div>
            <button onClick={() => doDelete(r.id)} className="p-1.5 text-gray-500 hover:text-red-400 rounded-lg hover:bg-gray-800"><Trash2 size={14} /></button>
          </div>
        ))}
        {reminders.length === 0 && <p className="text-gray-500 text-sm text-center py-8">No reminders.</p>}
      </div>
    </div>
  );
}
