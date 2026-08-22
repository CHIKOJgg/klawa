import { useEffect, useState } from 'react';
import { useAuth } from './lib/useAuth';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { Dashboard } from './pages/Dashboard';
import { TasksPage } from './pages/TasksPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { ChatPage } from './pages/ChatPage';
import { RemindersPage } from './pages/RemindersPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { MemoryPage } from './pages/MemoryPage';
import { Layout } from './components/Layout';

type AppPage = 'login' | 'register' | 'dashboard' | 'tasks' | 'projects' | 'chat' | 'reminders' | 'notifications' | 'memory';

function App() {
  const { token, loadUser } = useAuth();
  const [page, setPage] = useState<AppPage>('dashboard');

  useEffect(() => {
    if (token) loadUser();
  }, [token]);

  if (!token) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center">
        <div className="w-full max-w-md">
          {page === 'register' ? (
            <RegisterPage onSwitch={() => setPage('login')} onDone={() => setPage('login')} />
          ) : (
            <LoginPage onSwitch={() => setPage('register')} onDone={() => setPage('dashboard')} />
          )}
        </div>
      </div>
    );
  }

  const renderPage = () => {
    switch (page) {
      case 'tasks': return <TasksPage />;
      case 'projects': return <ProjectsPage />;
      case 'chat': return <ChatPage />;
      case 'reminders': return <RemindersPage />;
      case 'notifications': return <NotificationsPage />;
      case 'memory': return <MemoryPage />;
      default: return <Dashboard />;
    }
  };

  return             <Layout current={page as any} onChange={setPage as any}>{renderPage()}</Layout>;
}

export default App;
