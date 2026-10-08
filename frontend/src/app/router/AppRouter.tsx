import { lazy, Suspense, useEffect } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { AdminLayout } from '../../layouts/AdminLayout'
import { RecruiterLayout } from '../../layouts/RecruiterLayout'

const HomePage = lazy(() => import('../../pages/HomePage').then((module) => ({ default: module.HomePage })))
const LoginPage = lazy(() => import('../../pages/LoginPage').then((module) => ({ default: module.LoginPage })))
const NotFoundPage = lazy(() => import('../../pages/NotFoundPage').then((module) => ({ default: module.NotFoundPage })))
const RegisterPage = lazy(() => import('../../pages/RegisterPage').then((module) => ({ default: module.RegisterPage })))
const ForgotPasswordPage = lazy(() => import('../../pages/ForgotPasswordPage').then((module) => ({ default: module.ForgotPasswordPage })))
const ResetPasswordPage = lazy(() => import('../../pages/ResetPasswordPage').then((module) => ({ default: module.ResetPasswordPage })))
const AdminCandidatesPage = lazy(() => import('../../pages/admin/AdminCandidatesPage').then((module) => ({ default: module.AdminCandidatesPage })))
const AdminRecruitersPage = lazy(() => import('../../pages/admin/AdminRecruitersPage').then((module) => ({ default: module.AdminRecruitersPage })))
const AdminCompaniesPage = lazy(() => import('../../pages/admin/AdminCompaniesPage').then((module) => ({ default: module.AdminCompaniesPage })))
const AdminJobsPage = lazy(() => import('../../pages/admin/AdminJobsPage').then((module) => ({ default: module.AdminJobsPage })))
const RecruiterProfilePage = lazy(() => import('../../pages/recruiter/RecruiterProfilePage').then((module) => ({ default: module.RecruiterProfilePage })))
const RecruiterCompanyPage = lazy(() => import('../../pages/recruiter/RecruiterCompanyPage').then((module) => ({ default: module.RecruiterCompanyPage })))
const RecruiterJoinCompanyPage = lazy(() => import('../../pages/recruiter/RecruiterJoinCompanyPage').then((module) => ({ default: module.RecruiterJoinCompanyPage })))
const RecruiterPeerApprovalPage = lazy(() => import('../../pages/recruiter/RecruiterPeerApprovalPage').then((module) => ({ default: module.RecruiterPeerApprovalPage })))
const RecruiterJobsPage = lazy(() => import('../../pages/recruiter/RecruiterJobsPage').then((module) => ({ default: module.RecruiterJobsPage })))
const RecruiterJobApplicantsPage = lazy(() => import('../../pages/recruiter/RecruiterJobApplicantsPage').then((module) => ({ default: module.RecruiterJobApplicantsPage })))
const CandidateProfilePage = lazy(() => import('../../pages/candidate/CandidateProfilePage').then((module) => ({ default: module.CandidateProfilePage })))
const CandidateApplicationsPage = lazy(() => import('../../pages/candidate/CandidateApplicationsPage').then((module) => ({ default: module.CandidateApplicationsPage })))
const JobDetailPage = lazy(() => import('../../pages/jobs/JobDetailPage').then((module) => ({ default: module.JobDetailPage })))
const JobSearchPage = lazy(() => import('../../pages/jobs/JobSearchPage').then((module) => ({ default: module.JobSearchPage })))

function RouteLoadingFallback() {
    return (
        <div className="grid min-h-[40vh] place-items-center text-sm font-medium text-slate-500" role="status">
            Đang tải trang...
        </div>
    )
}

function ScrollToTop() {
    const { pathname } = useLocation()

    useEffect(() => {
        window.scrollTo(0, 0)
    }, [pathname])

    return null
}

export function AppRouter() {
    return (
        <>
            <ScrollToTop />
            <Suspense fallback={<RouteLoadingFallback />}>
            <Routes>
            {/* Public and Standard User Routes */}
            <Route element={<MainLayout />}>
                <Route element={<HomePage />} index />
                <Route element={<LoginPage />} path="login" />
                <Route element={<RegisterPage />} path="register" />
                <Route element={<ForgotPasswordPage />} path="forgot-password" />
                <Route element={<ResetPasswordPage />} path="reset-password" />
                <Route element={<JobSearchPage />} path="jobs" />
                <Route element={<JobDetailPage />} path="jobs/:jobId" />
                <Route element={<CandidateProfilePage />} path="candidate/profile" />
                <Route element={<CandidateApplicationsPage />} path="candidate/applications" />
                <Route element={<NotFoundPage />} path="*" />
            </Route>

            {/* Recruiter Management Routes */}
            <Route element={<RecruiterLayout />} path="recruiter">
                <Route element={<Navigate replace to="/recruiter/profile" />} index />
                <Route element={<RecruiterProfilePage />} path="profile" />
                <Route element={<RecruiterJobsPage />} path="jobs" />
                <Route element={<RecruiterJobApplicantsPage />} path="jobs/:jobId/applicants" />
                <Route element={<RecruiterCompanyPage />} path="company" />
                <Route element={<RecruiterJoinCompanyPage />} path="join-company" />
                <Route element={<RecruiterPeerApprovalPage />} path="peer-approval" />
            </Route>

            {/* Admin Management Routes */}
            <Route element={<AdminLayout />} path="admin">
                <Route element={<Navigate replace to="/admin/candidates" />} index />
                <Route element={<AdminCandidatesPage />} path="candidates" />
                <Route element={<AdminRecruitersPage />} path="recruiters" />
                <Route element={<AdminCompaniesPage />} path="companies" />
                <Route element={<AdminJobsPage />} path="jobs" />
            </Route>
            </Routes>
            </Suspense>
        </>
    )
}
