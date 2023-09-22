package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.OpenChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;

/**
 * Describes a configuration of a message item view
 * since 3.3.0
 */
final public class MessageListUIParams {
    @NonNull
    private final MessageGroupType messageGroupType;
    private final boolean useMessageGroupUI;
    private final boolean useReverseLayout;
    private final boolean useQuotedView;
    private final boolean useMessageReceipt;
    @NonNull
    private final ChannelConfig channelConfig;
    @NonNull
    private final OpenChannelConfig openChannelConfig;

    private MessageListUIParams(@NonNull MessageGroupType messageGroupType,
                                boolean useMessageGroupUI,
                                boolean useReverseLayout,
                                boolean useQuotedView,
                                boolean useMessageReceipt,
                                @NonNull ChannelConfig channelConfig,
                                @NonNull OpenChannelConfig openChannelConfig) {
        this.messageGroupType = messageGroupType;
        this.useMessageGroupUI = useMessageGroupUI;
        this.useReverseLayout = useReverseLayout;
        this.useQuotedView = useQuotedView;
        this.useMessageReceipt = useMessageReceipt;
        this.channelConfig = channelConfig;
        this.openChannelConfig = openChannelConfig;
    }

    /**
     * Returns the type of message group UI.
     *
     * @return The value of {@link MessageGroupType}
     * since 3.3.0
     */
    @NonNull
    public MessageGroupType getMessageGroupType() {
        return messageGroupType;
    }

    /**
     * Returns whether the quoted view is used.
     *
     * @return <code>true</code> if the quoted view is used, <code>false</code> otherwise
     * since 3.3.0
     */
    public boolean shouldUseQuotedView() {
        return useQuotedView;
    }

    /**
     * Returns whether the message grouping is used.
     *
     * @return <code>true</code> if the message grouping is used, <code>false</code> otherwise
     * since 3.3.0
     */
    public boolean shouldUseMessageGroupUI() {
        return useMessageGroupUI;
    }

    /**
     * Returns whether the message list is reversed.
     *
     * @return <code>true</code> if the message list is reversed, <code>false</code> otherwise
     * since 3.3.0
     */
    public boolean shouldUseReverseLayout() {
        return useReverseLayout;
    }

    /**
     * Returns whether the status (read receipt, delivery receipt) of messages is shown.
     *
     * @return <code>true</code> if the message receipt is shown, <code>false</code> otherwise
     * since 3.3.0
     */
    public boolean shouldUseMessageReceipt() {
        return useMessageReceipt;
    }

    /**
     * Returns [ChannelConfig] that contains the configuration of the channel.
     *
     * @return The value of [ChannelConfig]
     * since 3.6.0
     * @see ChannelConfig
     */
    @NonNull
    public ChannelConfig getChannelConfig() {
        return channelConfig;
    }

    /**
     * Returns [OpenChannelConfig] that contains the configuration of the open channel.
     *
     * @return The value of [OpenChannelConfig]
     * since 3.6.0
     * @see OpenChannelConfig
     */
    @NonNull
    public OpenChannelConfig getOpenChannelConfig() {
        return openChannelConfig;
    }

    public static class Builder {
        @NonNull
        private MessageGroupType messageGroupType = MessageGroupType.GROUPING_TYPE_SINGLE;
        private boolean useMessageGroupUI = true;
        private boolean useReverseLayout = true;
        private boolean useQuotedView = false;
        private boolean useMessageReceipt = true;
        @NonNull
        private ChannelConfig channelConfig = UIKitConfig.getGroupChannelConfig();
        @NonNull
        private OpenChannelConfig openChannelConfig = UIKitConfig.getOpenChannelConfig();

        /**
         * Constructor
         * since 3.3.0
         */
        public Builder() {}

        /**
         * Constructor
         *
         * @param params The message draw parameter to be used as the base
         * since 3.3.0
         */
        public Builder(@NonNull MessageListUIParams params) {
            this.messageGroupType = params.messageGroupType;
            this.useMessageGroupUI = params.useMessageGroupUI;
            this.useReverseLayout = params.useReverseLayout;
            this.useQuotedView = params.useQuotedView;
            this.useMessageReceipt = params.useMessageReceipt;
            this.channelConfig = params.channelConfig;
            this.openChannelConfig = params.openChannelConfig;
        }

        /**
         * Sets the type of message group UI.
         *
         * @param messageGroupType The value of {@link MessageGroupType}
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setMessageGroupType(@NonNull MessageGroupType messageGroupType) {
            this.messageGroupType = messageGroupType;
            return this;
        }

        /**
         * Sets whether the quoted view is used.
         *
         * @param useQuotedView <code>true</code> if the quoted view is used, <code>false</code> otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseQuotedView(boolean useQuotedView) {
            this.useQuotedView = useQuotedView;
            return this;
        }

        /**
         * Sets whether the message grouping is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message grouping is used, <code>false</code> otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseMessageGroupUI(boolean useMessageGroupUI) {
            this.useMessageGroupUI = useMessageGroupUI;
            return this;
        }

        /**
         * Sets whether the message list is reversed.
         *
         * @param useReverseLayout <code>true</code> if the message list is reversed, <code>false</code> otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseReverseLayout(boolean useReverseLayout) {
            this.useReverseLayout = useReverseLayout;
            return this;
        }

        /**
         * Sets whether the status (read receipt, delivery receipt) of messages is shown.
         *
         * @param useMessageReceipt <code>true</code> if the message receipt is shown, <code>false</code> otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseMessageReceipt(boolean useMessageReceipt) {
            this.useMessageReceipt = useMessageReceipt;
            return this;
        }

        /**
         * Sets [ChannelConfig] that contains the configuration of the channel.
         * Use {@code UIKitConfig.groupChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val params = MessageListUIParams.Builder(CHANNEL_URL)
         *     .setChannelConfig(
         *         UIKitConfig.groupChannelConfig.clone().apply {
         *             this.enableMention = true
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param channelConfig The value of [ChannelConfig]
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         * @see ChannelConfig
         */
        @NonNull
        public Builder setChannelConfig(@NonNull ChannelConfig channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        /**
         * Sets [OpenChannelConfig] that contains the configuration of the open channel.
         * Use {@code UIKitConfig.openChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val params = MessageListUIParams.Builder(CHANNEL_URL)
         *     .setOpenChannelConfig(
         *         UIKitConfig.openChannelConfig.clone().apply {
         *             this.enableOgTag = false
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param openChannelConfig The value of [OpenChannelConfig]
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         * @see OpenChannelConfig
         */
        @NonNull
        public Builder setOpenChannelConfig(@NonNull OpenChannelConfig openChannelConfig) {
            this.openChannelConfig = openChannelConfig;
            return this;
        }

        /**
         * Builds an {@link MessageListUIParams} with the properties supplied to this builder.
         *
         * @return The {@link MessageListUIParams} from this builder instance.
         * since 3.3.0
         */
        @NonNull
        public MessageListUIParams build() {
            return new MessageListUIParams(
                this.messageGroupType,
                this.useMessageGroupUI,
                this.useReverseLayout,
                this.useQuotedView,
                this.useMessageReceipt,
                this.channelConfig,
                this.openChannelConfig
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageListUIParams)) return false;

        MessageListUIParams that = (MessageListUIParams) o;

        if (useMessageGroupUI != that.useMessageGroupUI) return false;
        if (useReverseLayout != that.useReverseLayout) return false;
        if (useQuotedView != that.useQuotedView) return false;
        if (useMessageReceipt != that.useMessageReceipt) return false;
        if (messageGroupType != that.messageGroupType) return false;
        if (!channelConfig.equals(that.channelConfig)) return false;
        return openChannelConfig.equals(that.openChannelConfig);
    }

    @Override
    public int hashCode() {
        int result = messageGroupType.hashCode();
        result = 31 * result + (useMessageGroupUI ? 1 : 0);
        result = 31 * result + (useReverseLayout ? 1 : 0);
        result = 31 * result + (useQuotedView ? 1 : 0);
        result = 31 * result + (useMessageReceipt ? 1 : 0);
        result = 31 * result + channelConfig.hashCode();
        result = 31 * result + openChannelConfig.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MessageListUIParams{" +
            "messageGroupType=" + messageGroupType +
            ", useMessageGroupUI=" + useMessageGroupUI +
            ", useReverseLayout=" + useReverseLayout +
            ", useQuotedView=" + useQuotedView +
            ", useMessageReceipt=" + useMessageReceipt +
            ", channelConfig=" + channelConfig +
            ", openChannelConfig=" + openChannelConfig +
            '}';
    }
}
