import { Navigate, Route, Routes } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { AdminLayout } from '../../layouts/AdminLayout'
import { HomePage } from '../../pages/HomePage'
import { LoginPage } from '../../pages/LoginPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { RegisterPage } from '../../pages/RegisterPage'
import { ForgotPasswordPage } from '../../pages/ForgotPasswordPage'
import { ResetPasswordPage } from '../../pages/ResetPasswordPage'
import { AdminCandidatesPage } from '../../pages/admin/AdminCandidatesPage'
import { AdminRecruitersPage } from '../../pages/admin/AdminRecruitersPage'
import { AdminCompaniesPage } from '../../pages/admin/AdminCompaniesPage'

export function AppRouter() {
    return (
        <Routes>
            {/* Public and Standard User Routes */}
            <Route element={<MainLayout />}>
                <Route element={<HomePage />} index />
                <Route element={<LoginPage />} path="login" />
                <Route element={<RegisterPage />} path="register" />
                <Route element={<ForgotPasswordPage />} path="forgot-password" />
                <Route element={<ResetPasswordPage />} path="reset-password" />
                <Route element={<NotFoundPage />} path="*" />
            </Route>

            {/* Admin Management Routes */}
            <Route element={<AdminLayout />} path="admin">
                <Route element={<Navigate replace to="/admin/candidates" />} index />
                <Route element={<AdminCandidatesPage />} path="candidates" />
                <Route element={<AdminRecruitersPage />} path="recruiters" />
                <Route element={<AdminCompaniesPage />} path="companies" />
            </Route>
        </Routes>
    )
}