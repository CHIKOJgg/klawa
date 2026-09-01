import { useState } from 'react';
import { useAuth } from '../lib/useAuth';
import { UserPlus } from 'lucide-react';

export function RegisterPage({ onSwitch, onDone }: { onSwitch: () => void; onDone: () => void }) {
  const { register } = useAuth();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [err, setErr] = useState('');
  const [ok, setOk] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr('');
    try { await register(form.firstName, form.lastName, form.email, form.password); setOk(true); }
    catch (e: any) { setErr(e.message); }
  };

  if (ok) return (
    <div className="bg-gray-900 rounded-xl p-8 border border-gray-800 shadow-xl text-center">
      <div className="text-green-400 text-5xl mb-4">✓</div>
      <h2 className="text-xl font-bold text-white mb-2">Registered!</h2>
      <p className="text-gray-400 mb-4">You can now sign in.</p>
      <button onClick={onDone} className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium px-6 py-2 rounded-lg">Sign In</button>
    </div>
  );

  const set = (k: string) => (v: string) => setForm(f => ({ ...f, [k]: v }));

  return (
    <form onSubmit={handleSubmit} className="bg-gray-900 rounded-xl p-8 border border-gray-800 shadow-xl">
      <div className="flex items-center gap-3 mb-6">
        <UserPlus className="text-indigo-400" size={28} />
        <h2 className="text-2xl font-bold text-white">Register</h2>
      </div>
      {err && <div className="bg-red-900/50 text-red-300 text-sm p-3 rounded-lg mb-4">{err}</div>}
      <div className="space-y-4">
        {['firstName','lastName','email','password'].map(f => (
          <div key={f}>
            <label className="block text-sm text-gray-400 mb-1 capitalize">{f === 'firstName' ? 'First Name' : f === 'lastName' ? 'Last Name' : f}</label>
            <input type={f === 'password' ? 'password' : f === 'email' ? 'email' : 'text'} value={(form as any)[f]} onChange={e => set(f)(e.target.value)} required
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500" />
          </div>
        ))}
        <button type="submit" className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded-lg transition-colors">Register</button>
      </div>
      <p className="text-sm text-gray-500 mt-4 text-center">
        Already have an account? <button type="button" onClick={onSwitch} className="text-indigo-400 hover:underline">Sign In</button>
      </p>
    </form>
  );
}
