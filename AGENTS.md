# TalentBridge Project - Agent Guidelines & Rules

## 1. Context Management: Working Capsule Protocol
Whenever context is truncated, overloaded, or when summarizing work status:
The agent MUST compress the state into a concise **WORKING CAPSULE** with exactly 8 fields:

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

## 2. Research Grounding (SoftwareX 2025: AI-KM)
- Ground AI enhancements in the research paper *"Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow"* (SoftwareX 31 (2025) 102349, DOI: `10.1016/j.softx.2025.102349`).
- Utilize Inferred Hidden Information (Reverse Question/Skill Generation).
- Apply Tri-Vector Hybrid Similarity for matching algorithms.
- Unify RAG (Static & Knowledge base retrieval) with MCP (Model Context Protocol for dynamic tool execution and DB actions).

## 3. Engineering Rigor
- Read source code, docs, .md, and README before executing tasks.
- Maintain strict typing, security, backward compatibility, and atomic git commits.

## 4. Progress Reporting Rule (Mandatory Report File)
Sau mỗi khi hoàn thành nhiệm vụ, Agent **BẮT BUỘC** cập nhật vào file báo cáo `docs/PROGRESS_REPORT.md` với cấu trúc ngắn gọn:
- **Đã làm được gì**: Tóm tắt danh mục công việc đã hoàn thành và kiểm thử.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Liệt kê rõ công nghệ, thư viện, thuật toán và sự phối hợp.
- **Mục đích chức năng / việc đã làm**: Nêu rõ giá trị nghiệp vụ và giải quyết bài toán gì.

