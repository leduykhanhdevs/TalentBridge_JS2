import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { AppProviders } from './app/providers/AppProviders'
import { ErrorBoundary } from './components/ErrorBoundary'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ErrorBoundary fallback={(
            <main className="grid min-h-screen place-items-center bg-slate-50 px-4 text-center">
                <section className="max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
                    <h1 className="text-xl font-bold text-slate-900">TalentBridge gặp sự cố khi hiển thị trang</h1>
                    <p className="mt-2 text-sm text-slate-600">Tải lại trang để thử khôi phục phiên làm việc của bạn.</p>
                    <button className="mt-5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white" onClick={() => window.location.reload()} type="button">
                        Tải lại trang
                    </button>
                </section>
            </main>
        )}>
            <AppProviders>
                <App />
            </AppProviders>
        </ErrorBoundary>
    </StrictMode>,
)
