import { useState } from 'react';
import { api } from '../lib/api';
import { Send, Bot } from 'lucide-react';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

export function ChatPage() {
  const [messages, setMessages] = useState<Message[]>([{ role: 'assistant', content: 'Hello! How can I help you today?' }]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);

  const send = async () => {
    if (!input.trim()) return;
    const userMsg = { role: 'user' as const, content: input };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setSending(true);
    try {
      const res = await api.chat.send(input);
      setMessages(prev => [...prev, { role: 'assistant', content: res?.response || 'OK' }]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Error: could not reach AI agent.' }]);
    }
    setSending(false);
  };

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)]">
      <h1 className="text-2xl font-bold text-white mb-4">AI Chat</h1>
      <div className="flex-1 overflow-y-auto space-y-3 mb-4">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] rounded-xl px-4 py-3 ${m.role === 'user' ? 'bg-indigo-600 text-white' : 'bg-gray-800 text-gray-200'}`}>
              {m.role === 'assistant' && <Bot size={14} className="text-indigo-400 mb-1" />}
              <p className="text-sm whitespace-pre-wrap">{m.content}</p>
            </div>
          </div>
        ))}
        {sending && <div className="text-gray-500 text-sm animate-pulse">Thinking...</div>}
      </div>
      <div className="flex gap-2">
        <input value={input} onChange={e => setInput(e.target.value)} onKeyDown={e => e.key === 'Enter' && send()} placeholder="Type a message..."
          className="flex-1 bg-gray-800 border border-gray-700 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-indigo-500" />
        <button onClick={send} disabled={sending || !input.trim()}
          className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white p-3 rounded-xl transition-colors">
          <Send size={18} />
        </button>
      </div>
    </div>
  );
}
