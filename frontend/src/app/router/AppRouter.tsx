import { useEffect } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router'
import { MainLayout } from '../../layouts/MainLayout'
import { AdminLayout } from '../../layouts/AdminLayout'
import { RecruiterLayout } from '../../layouts/RecruiterLayout'
import { HomePage } from '../../pages/HomePage'
import { LoginPage } from '../../pages/LoginPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { RegisterPage } from '../../pages/RegisterPage'
import { ForgotPasswordPage } from '../../pages/ForgotPasswordPage'
import { ResetPasswordPage } from '../../pages/ResetPasswordPage'
import { AdminCandidatesPage } from '../../pages/admin/AdminCandidatesPage'
import { AdminRecruitersPage } from '../../pages/admin/AdminRecruitersPage'
import { AdminCompaniesPage } from '../../pages/admin/AdminCompaniesPage'
import { AdminJobsPage } from '../../pages/admin/AdminJobsPage'
import { RecruiterProfilePage } from '../../pages/recruiter/RecruiterProfilePage'
import { RecruiterCompanyPage } from '../../pages/recruiter/RecruiterCompanyPage'
import { RecruiterJoinCompanyPage } from '../../pages/recruiter/RecruiterJoinCompanyPage'
import { RecruiterPeerApprovalPage } from '../../pages/recruiter/RecruiterPeerApprovalPage'
import { RecruiterJobsPage } from '../../pages/recruiter/RecruiterJobsPage'
import { RecruiterJobApplicantsPage } from '../../pages/recruiter/RecruiterJobApplicantsPage'
import { CandidateProfilePage } from '../../pages/candidate/CandidateProfilePage'
import { CandidateApplicationsPage } from '../../pages/candidate/CandidateApplicationsPage'
import { JobDetailPage } from '../../pages/jobs/JobDetailPage'
import { JobSearchPage } from '../../pages/jobs/JobSearchPage'

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
        </>
    )
}
