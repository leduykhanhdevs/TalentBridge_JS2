import type { AiMatchResult } from '../recruiter/recruiterApplicantTypes'

export interface JobQualityCriterion {
    name: string
    points: number
    maximumPoints: number
    satisfied: boolean
    evidence: string
}

export interface JobQualityReport {
    jobId: number
    jobTitle: string
    jobStatus: string
    qualityScore: number
    qualityLabel: string
    criteria: JobQualityCriterion[]
    trustSignals: string[]
    improvementSuggestions: string[]
    limitation: string
}

export interface PipelineStageStatistic {
    stage: string
    applicantCount: number
    sharePercentage: number
}

export interface RecruiterPipelineAnalytics {
    jobId: number
    jobTitle: string
    totalApplications: number
    stages: PipelineStageStatistic[]
    interpretation: string
}

export interface CandidateJobAssessment {
    match: AiMatchResult
    profileCompletenessPercentage: number
    missingProfileSections: string[]
    candidateOverallAssessment: string
    advisoryNotice: string
}
