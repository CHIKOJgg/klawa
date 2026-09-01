import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { Plus, Trash2, Search } from 'lucide-react';

export function MemoryPage() {
  const [memories, setMemories] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ key: '', value: '', source: '' });
  const [search, setSearch] = useState('');

  const load = () => {
    if (search) api.memories.search(search).then(setMemories).catch(() => {});
    else api.memories.list().then(setMemories).catch(() => {});
  };

  useEffect(() => { load(); }, []);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.memories.create(form);
    setShowForm(false); setForm({ key: '', value: '', source: '' });
    load();
  };

  const doDelete = async (id: number) => { await api.memories.delete(id); load(); };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Memory</h1>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-lg">
          <Plus size={16} /> Add Memory
        </button>
      </div>
      <div className="relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
        <input value={search} onChange={e => { setSearch(e.target.value); if (!e.target.value) load(); }} placeholder="Search memories..."
          className="w-full bg-gray-800 border border-gray-700 rounded-xl pl-10 pr-4 py-2 text-white text-sm" />
      </div>
      {showForm && (
        <form onSubmit={create} className="bg-gray-900 rounded-xl p-4 border border-gray-800 space-y-3">
          <input value={form.key} onChange={e => setForm(f => ({...f, key: e.target.value}))} placeholder="Key" required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <input value={form.value} onChange={e => setForm(f => ({...f, value: e.target.value}))} placeholder="Value" required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <input value={form.source} onChange={e => setForm(f => ({...f, source: e.target.value}))} placeholder="Source"
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <div className="flex gap-2">
            <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm">Save</button>
            <button type="button" onClick={() => setShowForm(false)} className="text-gray-400 hover:text-white px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
      <div className="space-y-2">
        {memories.map((m: any) => (
          <div key={m.id} className="flex items-center justify-between bg-gray-900 rounded-xl p-4 border border-gray-800">
            <div className="min-w-0 flex-1">
              <p className="text-sm font-medium text-white">{m.key}</p>
              <p className="text-xs text-gray-400 mt-0.5">{m.value}</p>
              {m.source && <p className="text-xs text-gray-600 mt-0.5">Source: {m.source}</p>}
            </div>
            <button onClick={() => doDelete(m.id)} className="p-1.5 text-gray-500 hover:text-red-400 rounded-lg hover:bg-gray-800 ml-2"><Trash2 size={14} /></button>
          </div>
        ))}
        {memories.length === 0 && <p className="text-gray-500 text-sm text-center py-8">No memories stored.</p>}
      </div>
    </div>
  );
}
