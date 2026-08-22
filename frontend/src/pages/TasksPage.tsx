import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { Plus, Check, Archive, Trash2 } from 'lucide-react';

export function TasksPage() {
  const [tasks, setTasks] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', dueDate: '' });

  useEffect(() => { api.tasks.list().then(setTasks).catch(() => {}); }, []);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.tasks.create({ ...form, dueDate: form.dueDate ? new Date(form.dueDate).toISOString() : null });
    setShowForm(false);
    setForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '' });
    api.tasks.list().then(setTasks);
  };

  const toggleComplete = async (t: any) => {
    if (t.status === 'COMPLETED') await api.tasks.reopen(t.id);
    else await api.tasks.complete(t.id);
    api.tasks.list().then(setTasks);
  };

  const doArchive = async (id: number) => { await api.tasks.archive(id); api.tasks.list().then(setTasks); };
  const doDelete = async (id: number) => { await api.tasks.delete(id); api.tasks.list().then(setTasks); };

  const priorityColor = (p: string) =>
    p === 'URGENT' ? 'text-red-400' : p === 'HIGH' ? 'text-orange-400' : p === 'MEDIUM' ? 'text-yellow-400' : 'text-gray-400';

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Tasks</h1>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-lg transition-colors">
          <Plus size={16} /> New Task
        </button>
      </div>
      {showForm && (
        <form onSubmit={create} className="bg-gray-900 rounded-xl p-4 border border-gray-800 space-y-3">
          <input value={form.title} onChange={e => setForm(f => ({...f, title: e.target.value}))} placeholder="Title" required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <textarea value={form.description} onChange={e => setForm(f => ({...f, description: e.target.value}))} placeholder="Description" rows={3}
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <div className="flex gap-3">
            <select value={form.priority} onChange={e => setForm(f => ({...f, priority: e.target.value}))}
              className="bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm">
              <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option><option value="URGENT">Urgent</option>
            </select>
            <input type="date" value={form.dueDate} onChange={e => setForm(f => ({...f, dueDate: e.target.value}))}
              className="bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
            <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="text-gray-400 hover:text-white px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
      <div className="space-y-2">
        {tasks.map((t: any) => (
          <div key={t.id} className={`bg-gray-900 rounded-xl p-4 border ${t.status === 'COMPLETED' ? 'border-green-800/50' : 'border-gray-800'}`}>
            <div className="flex items-start justify-between gap-4">
              <div className="flex items-start gap-3 flex-1 min-w-0">
                <button onClick={() => toggleComplete(t)} className={`mt-0.5 w-5 h-5 rounded border-2 flex-shrink-0 flex items-center justify-center ${t.status === 'COMPLETED' ? 'bg-green-600 border-green-600' : 'border-gray-600 hover:border-indigo-500'}`}>
                  {t.status === 'COMPLETED' && <Check size={12} className="text-white" />}
                </button>
                <div className="min-w-0">
                  <p className={`text-sm font-medium ${t.status === 'COMPLETED' ? 'line-through text-gray-500' : 'text-white'}`}>{t.title}</p>
                  {t.description && <p className="text-xs text-gray-500 mt-1 truncate">{t.description}</p>}
                  <div className="flex items-center gap-3 mt-2">
                    <span className={`text-xs font-medium ${priorityColor(t.priority)}`}>{t.priority}</span>
                    {t.dueDate && <span className="text-xs text-gray-500">{new Date(t.dueDate).toLocaleDateString()}</span>}
                    <span className="text-xs text-gray-500">{t.status}</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-1 flex-shrink-0">
                {t.status !== 'ARCHIVED' && <button onClick={() => doArchive(t.id)} className="p-1.5 text-gray-500 hover:text-white rounded-lg hover:bg-gray-800"><Archive size={14} /></button>}
                <button onClick={() => doDelete(t.id)} className="p-1.5 text-gray-500 hover:text-red-400 rounded-lg hover:bg-gray-800"><Trash2 size={14} /></button>
              </div>
            </div>
          </div>
        ))}
        {tasks.length === 0 && <p className="text-gray-500 text-sm text-center py-8">No tasks yet. Create one!</p>}
      </div>
    </div>
  );
}
