---
name: knowledge-capsule
description: >-
  Knowledge Engineering and Context Compression Skill based on the SoftwareX 2025 paper
  "AI-KM: Knowledge enhancement with RAG and workflow" (Wu et al.). Enforces the 8-section
  WORKING CAPSULE protocol, Inferred Hidden Information, and Hybrid RAG/MCP architecture.
---

# Knowledge Capsule & Research-Driven Workflow Skill

This skill operationalizes the scientific methodology of:
**"Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow"** (SoftwareX 31, 2025, 102349).

## 1. Mandatory Context Compression Protocol (WORKING CAPSULE)
Whenever the context window approaches truncation, becomes fragmented, or when summarizing work:
Compress the state into exactly 8 sections:
1. **OBJECTIVE**: Specific current goal.
2. **DONE**: What has been implemented and verified.
3. **CONSTRAINTS**: Key operational and technical constraints.
4. **DECISIONS**: Finalized decisions that should not be reopened without justification.
5. **FILES/ARTIFACTS**: Direct code files and documents in scope.
6. **ERRORS/SCARS**: Experienced failures, solutions, and rules to prevent recurrence.
7. **OPEN ITEMS**: Incomplete items and pending user clarifications.
8. **NEXT ACTION**: Single immediate executable action.

## 2. Inferred Hidden Information (Reverse Reasoning)
When analyzing unstructured inputs (such as CVs, Resumes, or Job Descriptions):
- Do not rely solely on naive literal keyword matching.
- Generate potential inferred questions and capability dimensions $V = \{v_1, \dots, v_n\}$ that the document addresses.
- Evaluate candidate-job fit using Tri-Vector Hybrid Similarity:
  $$Merge(q, v_i, u_i) = \frac{\cos(q, v_i) + \cos(q, u_i)}{2}$$

## 3. Unified RAG + MCP Architecture
- **RAG Tier**: For static/semi-static semantic indexing (CV profiles, job descriptions, policy docs).
- **MCP Tier**: For dynamic real-time system interactions (Candidate database queries, application status transition, notification dispatching, calendar scheduling).
