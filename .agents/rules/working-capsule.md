# MANDATORY CONTEXT COMPRESSION & WORKING CAPSULE RULE

Whenever context gets truncated, summarized, overloaded, or when handing over tasks:
The agent MUST compress the state into a concise WORKING CAPSULE with exactly 8 fields:

- **OBJECTIVE**: Current specific goal.
- **DONE**: Completed and verified tasks.
- **CONSTRAINTS**: Key constraints and non-negotiable rules.
- **DECISIONS**: Finalized technical/architectural decisions.
- **FILES/ARTIFACTS**: Relevant active files and code locations.
- **ERRORS/SCARS**: Experienced errors, root cause, resolution, and what MUST NEVER be repeated.
- **OPEN ITEMS**: Pending tasks and questions.
- **NEXT ACTION**: Single concrete next action.

Prune strictly:
- Repetitive conversations
- Outdated long explanations
- Rejected alternative designs
- Long logs/traces
- Code content readable directly from files

Always read existing source code, docs, .md, and README before executing tasks.
Grounded in research paper "AI-KM: Knowledge enhancement with RAG and workflow" (SoftwareX 2025).

Mandatory Progress Reporting:
Sau mỗi khi hoàn thành nhiệm vụ, Agent BẮT BUỘC cập nhật file `docs/PROGRESS_REPORT.md` (Đã làm được gì | Công nghệ / thuật toán | Mục đích chức năng).

