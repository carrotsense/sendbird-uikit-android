package com.sendbird.uikit.activities.viewholder;

import androidx.annotation.NonNull;

/**
 * Represents type of messages.
 */
public enum MessageType {
    /**
     * Type of plain text message sent by the current user.
     */
    VIEW_TYPE_USER_MESSAGE_ME(0),
    /**
     * Type of plain text message sent by users other than the current user.
     */
    VIEW_TYPE_USER_MESSAGE_OTHER(1),
    /**
     * Type of file message sent by the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_ME(2),
    /**
     * Type of file message sent by users other than the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_OTHER(3),
    /**
     * Type of image message sent by the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_IMAGE_ME(4),
    /**
     * Type of image message sent by users other than the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER(5),
    /**
     * Type of video message sent by the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_VIDEO_ME(6),
    /**
     * Type of video message sent by users other than the current user.
     */
    VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER(7),
    /**
     * Message type sent by the administrator.
     */
    VIEW_TYPE_ADMIN_MESSAGE(8),
    /**
     * Message type that displays the date.
     */
    VIEW_TYPE_TIME_LINE(9),
    /**
     * Type of unknown message sent by the current user.
     */
    VIEW_TYPE_UNKNOWN_MESSAGE_ME(10),
    /**
     * Type of unknown message sent by users other than the current user.
     */
    VIEW_TYPE_UNKNOWN_MESSAGE_OTHER(11),
    /**
     * Type of a parent message info in thread list.
     * This type is only used in <code>ThreadListAdapter</code>
     *
     * @since 3.3.0
     */
    VIEW_TYPE_PARENT_MESSAGE_INFO(12),
    /**
     * Type of notification channel's message sent by the administrator.
     *
     * @since 3.5.0
     */
    VIEW_TYPE_NOTIFICATION_CHANNEL_MESSAGE(13);

    final int value;
    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    public static MessageType from(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return VIEW_TYPE_ADMIN_MESSAGE;
    }
}
