import { Route, Routes } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { HomePage } from '../../pages/HomePage'
import { LoginPage } from '../../pages/LoginPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { RegisterPage } from '../../pages/RegisterPage'
import { RoleHomePage } from '../../pages/RoleHomePage'

export function AppRouter() {
    return (
        <Routes>
            <Route element={<MainLayout />}>
                <Route element={<HomePage />} index />
                <Route element={<LoginPage />} path="login" />
                <Route element={<RegisterPage />} path="register" />
                <Route
                    element={<RoleHomePage role="ROLE_CANDIDATE" />}
                    path="candidate"
                />
                <Route
                    element={<RoleHomePage role="ROLE_RECRUITER" />}
                    path="recruiter"
                />
                <Route
                    element={<RoleHomePage role="ROLE_ADMIN" />}
                    path="admin"
                />
                <Route element={<NotFoundPage />} path="*" />
            </Route>
        </Routes>
    )
}
