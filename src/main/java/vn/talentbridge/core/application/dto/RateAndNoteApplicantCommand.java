package vn.talentbridge.core.application.dto;

public class RateAndNoteApplicantCommand {
    private Integer rating;
    private String tag;
    private String comment;

    public RateAndNoteApplicantCommand() {
    }

    public RateAndNoteApplicantCommand(Integer rating, String tag, String comment) {
        this.rating = rating;
        this.tag = tag;
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
