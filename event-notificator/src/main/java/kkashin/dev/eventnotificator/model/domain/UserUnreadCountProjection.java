package kkashin.dev.eventnotificator.model.domain;

public interface UserUnreadCountProjection {
    Long getUserId();
    Long getUnreadCount();
}
