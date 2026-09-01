import { useState } from 'react';
import { useAuth } from '../lib/useAuth';
import { LogIn } from 'lucide-react';

export function LoginPage({ onSwitch, onDone }: { onSwitch: () => void; onDone: () => void }) {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [err, setErr] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr('');
    try { await login(email, password); onDone(); }
    catch (e: any) { setErr(e.message); }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-gray-900 rounded-xl p-8 border border-gray-800 shadow-xl">
      <div className="flex items-center gap-3 mb-6">
        <LogIn className="text-indigo-400" size={28} />
        <h2 className="text-2xl font-bold text-white">Sign In</h2>
      </div>
      {err && <div className="bg-red-900/50 text-red-300 text-sm p-3 rounded-lg mb-4">{err}</div>}
      <div className="space-y-4">
        <div>
          <label className="block text-sm text-gray-400 mb-1">Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500" />
        </div>
        <div>
          <label className="block text-sm text-gray-400 mb-1">Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500" />
        </div>
        <button type="submit" className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded-lg transition-colors">Sign In</button>
      </div>
      <p className="text-sm text-gray-500 mt-4 text-center">
        No account? <button type="button" onClick={onSwitch} className="text-indigo-400 hover:underline">Register</button>
      </p>
    </form>
  );
}
