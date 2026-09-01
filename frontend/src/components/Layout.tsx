import { useAuth } from '../lib/useAuth';
import { LayoutDashboard, ListTodo, FolderKanban, MessageSquare, Bell, Clock, Database, LogOut, Menu, X } from 'lucide-react';
import { useState } from 'react';

const navItems = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'tasks', label: 'Tasks', icon: ListTodo },
  { id: 'projects', label: 'Projects', icon: FolderKanban },
  { id: 'chat', label: 'AI Chat', icon: MessageSquare },
  { id: 'reminders', label: 'Reminders', icon: Clock },
  { id: 'notifications', label: 'Notifications', icon: Bell },
  { id: 'memory', label: 'Memory', icon: Database },
];

type Page = 'dashboard' | 'tasks' | 'projects' | 'chat' | 'reminders' | 'notifications' | 'memory';

export function Layout({ current, onChange, children }: { current: Page; onChange: (p: Page) => void; children: React.ReactNode }) {
  const { logout } = useAuth();
  const [open, setOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-950 flex">
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-gray-900 border-r border-gray-800 transform ${open ? 'translate-x-0' : '-translate-x-full'} lg:relative lg:translate-x-0 transition-transform`}>
        <div className="flex items-center justify-between p-4 border-b border-gray-800">
          <h1 className="text-lg font-bold text-white">Klawa AI</h1>
          <button className="lg:hidden text-gray-400" onClick={() => setOpen(false)}><X size={20} /></button>
        </div>
        <nav className="p-2 space-y-1">
          {navItems.map(({ id, label, icon: Icon }) => (
            <button key={id} onClick={() => { onChange(id as Page); setOpen(false); }}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${current === id ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:text-white hover:bg-gray-800'}`}>
              <Icon size={18} /> {label}
            </button>
          ))}
        </nav>
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-800">
          <button onClick={logout} className="flex items-center gap-3 text-sm text-gray-400 hover:text-white w-full px-3 py-2">
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>
      {open && <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={() => setOpen(false)} />}
      <div className="flex-1 flex flex-col min-h-screen">
        <header className="lg:hidden flex items-center justify-between p-4 bg-gray-900 border-b border-gray-800">
          <button onClick={() => setOpen(true)} className="text-gray-400"><Menu size={24} /></button>
          <h1 className="text-lg font-bold text-white">Klawa AI</h1>
          <div className="w-6" />
        </header>
        <main className="flex-1 p-4 lg:p-6 overflow-auto">{children}</main>
      </div>
    </div>
  );
}
