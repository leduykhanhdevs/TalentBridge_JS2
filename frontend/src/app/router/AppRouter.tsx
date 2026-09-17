import { Route, Routes } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { HomePage } from '../../pages/HomePage'
import { LoginPage } from '../../pages/LoginPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { RegisterPage } from '../../pages/RegisterPage'
import { ForgotPasswordPage } from '../../pages/ForgotPasswordPage'
import { ResetPasswordPage } from '../../pages/ResetPasswordPage'

export function AppRouter() {
    return (
        <Routes>
            <Route element={<MainLayout />}>
                <Route element={<HomePage />} index />
                <Route element={<LoginPage />} path="login" />
                <Route element={<RegisterPage />} path="register" />
                <Route element={<ForgotPasswordPage />} path="forgot-password" />
                <Route element={<ResetPasswordPage />} path="reset-password" />
                <Route element={<NotFoundPage />} path="*" />
            </Route>
        </Routes>
    )
}