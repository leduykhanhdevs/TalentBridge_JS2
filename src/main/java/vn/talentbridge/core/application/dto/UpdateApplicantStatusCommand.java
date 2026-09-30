package vn.talentbridge.core.application.dto;

public class UpdateApplicantStatusCommand {
    private String stage;
    private String status;
    private String note;

    public UpdateApplicantStatusCommand() {
    }

    public UpdateApplicantStatusCommand(String stage, String status, String note) {
        this.stage = stage;
        this.status = status;
        this.note = note;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
