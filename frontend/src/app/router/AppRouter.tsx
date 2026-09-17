import { Route, Routes } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { HomePage } from '../../pages/HomePage'
import { LoginPage } from '../../pages/LoginPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { RegisterPage } from '../../pages/RegisterPage'

export function AppRouter() {
    return (
        <Routes>
            <Route element={<MainLayout />}>
                <Route element={<HomePage />} index />
                <Route element={<LoginPage />} path="login" />
                <Route element={<RegisterPage />} path="register" />
                <Route element={<NotFoundPage />} path="*" />
            </Route>
        </Routes>
    )
}