package vn.talentbridge.adapter.in.web.dto.response;

import vn.talentbridge.core.application.dto.MyJobStatsResult;

public record MyJobStatsResponse(long total, long draft, long pending, long active,
                                 long rejected, long expired, long closed) {
    public static MyJobStatsResponse from(MyJobStatsResult result) {
        return new MyJobStatsResponse(result.total(), result.draft(), result.pending(), result.active(),
                result.rejected(), result.expired(), result.closed());
    }
}
