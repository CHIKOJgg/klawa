import { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { Plus, Trash2, ChevronDown, ChevronRight } from 'lucide-react';

export function ProjectsPage() {
  const [projects, setProjects] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [desc, setDesc] = useState('');
  const [expanded, setExpanded] = useState<number | null>(null);
  const [projectTasks, setProjectTasks] = useState<any[]>([]);

  useEffect(() => { api.projects.list().then(setProjects).catch(() => {}); }, []);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.projects.create({ name, description: desc });
    setShowForm(false); setName(''); setDesc('');
    api.projects.list().then(setProjects);
  };

  const doDelete = async (id: number) => { await api.projects.delete(id); setProjects(p => p.filter(x => x.id !== id)); };

  const toggleExpand = async (id: number) => {
    if (expanded === id) { setExpanded(null); return; }
    setExpanded(id);
    const tasks = await api.projects.tasks(id).catch(() => []);
    setProjectTasks(tasks);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Projects</h1>
        <button onClick={() => setShowForm(!showForm)} className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-lg">
          <Plus size={16} /> New Project
        </button>
      </div>
      {showForm && (
        <form onSubmit={create} className="bg-gray-900 rounded-xl p-4 border border-gray-800 space-y-3">
          <input value={name} onChange={e => setName(e.target.value)} placeholder="Project name" required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <textarea value={desc} onChange={e => setDesc(e.target.value)} placeholder="Description" rows={2}
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm" />
          <div className="flex gap-2">
            <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="text-gray-400 hover:text-white px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
      <div className="space-y-2">
        {projects.map((p: any) => (
          <div key={p.id} className="bg-gray-900 rounded-xl border border-gray-800">
            <div className="flex items-center justify-between p-4">
              <button onClick={() => toggleExpand(p.id)} className="flex items-center gap-2 text-left flex-1">
                {expanded === p.id ? <ChevronDown size={16} className="text-gray-400" /> : <ChevronRight size={16} className="text-gray-400" />}
                <div>
                  <p className="text-sm font-medium text-white">{p.name}</p>
                  {p.description && <p className="text-xs text-gray-500 mt-0.5">{p.description}</p>}
                </div>
              </button>
              <button onClick={() => doDelete(p.id)} className="p-1.5 text-gray-500 hover:text-red-400 rounded-lg hover:bg-gray-800"><Trash2 size={14} /></button>
            </div>
            {expanded === p.id && (
              <div className="px-4 pb-4 pt-0 border-t border-gray-800">
                <p className="text-xs text-gray-500 mb-2 mt-3">Tasks in this project:</p>
                {projectTasks.length === 0 ? (
                  <p className="text-xs text-gray-600">No tasks</p>
                ) : projectTasks.map((t: any) => (
                  <div key={t.id} className="flex items-center justify-between bg-gray-800 rounded-lg px-3 py-2 mb-1">
                    <span className="text-sm text-white">{t.title}</span>
                    <span className="text-xs text-gray-400">{t.status}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
        {projects.length === 0 && <p className="text-gray-500 text-sm text-center py-8">No projects yet.</p>}
      </div>
    </div>
  );
}
