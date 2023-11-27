package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.collection.CollectionEventSource;
import com.sendbird.android.collection.GroupChannelContext;
import com.sendbird.android.collection.MessageCollectionInitPolicy;
import com.sendbird.android.collection.MessageContext;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.android.handler.GroupChannelHandler;
import com.sendbird.android.handler.MessageCollectionHandler;
import com.sendbird.android.handler.MessageCollectionInitHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.params.MessageCollectionCreateParams;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.common.MessagePayloadFilter;
import com.sendbird.android.user.User;
import com.sendbird.uikit.consts.MessageLoadState;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnCompleteHandler;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
import com.sendbird.uikit.internal.wrappers.MessageCollectionImpl;
import com.sendbird.uikit.internal.wrappers.MessageCollectionWrapper;
import com.sendbird.uikit.internal.wrappers.SendbirdChatImpl;
import com.sendbird.uikit.internal.wrappers.SendbirdChatWrapper;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitImpl;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitWrapper;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.SuggestedRepliesMessage;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.widgets.StatusFrameView;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of messages in a channel
 *
 * since 3.0.0
 */
public class ChannelViewModel extends BaseMessageListViewModel {
    @NonNull
    private final String ID_CHANNEL_EVENT_HANDLER = "ID_CHANNEL_EVENT_HANDLER" + System.currentTimeMillis();
    @NonNull
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT" + System.currentTimeMillis();
    @NonNull
    private final MutableLiveData<List<User>> typingMembers = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<BaseMessage>> messagesDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> hugeGapDetected = new MutableLiveData<>();
    @Nullable
    private MessageListParams messageListParams;
    @Nullable
    private MessageCollectionWrapper collection;
    @Nullable
    private MessageCollectionHandler handler;
    private boolean needToLoadMessageCache = true;
    private boolean isChatScreenVisible = false;
    @NonNull
    private final SendbirdChatWrapper sendbirdChatWrapper;
    @NonNull
    private final ChannelConfig channelConfig;

    /**
     * Class that holds message data in a channel.
     *
     * since 3.0.0
     */
    public static class ChannelMessageData {
        final List<BaseMessage> messages;
        final String traceName;

        ChannelMessageData(@Nullable String traceName, @NonNull List<BaseMessage> messages) {
            this.traceName = traceName;
            this.messages = messages;
        }

        /**
         * Returns a list of messages for the current channel.
         *
         * @return A list of the latest messages on the current channel
         * since 3.0.0
         */
        @NonNull
        public List<BaseMessage> getMessages() {
            return messages;
        }

        /**
         * Returns data indicating how the message list was updated.
         *
         * @return The String that traces the path of the message list
         * since 3.0.0
         */
        @Nullable
        public String getTraceName() {
            return traceName;
        }
    }

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param messageListParams Parameters required to retrieve the message list from this view model
     * since 3.0.0
     */
    public ChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
        this(channelUrl, messageListParams, UIKitConfig.getGroupChannelConfig());
    }

    ChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams, @NonNull ChannelConfig channelConfig) {
        this(channelUrl, messageListParams, new SendbirdUIKitImpl(), new SendbirdChatImpl(), channelConfig);
    }

    @VisibleForTesting
    ChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams, @NonNull SendbirdUIKitWrapper sendbirdUIKitWrapper, @NonNull SendbirdChatWrapper sendbirdChatWrapper, @NonNull ChannelConfig channelConfig) {
        super(channelUrl, sendbirdUIKitWrapper);
        this.messageListParams = messageListParams;
        this.sendbirdChatWrapper = sendbirdChatWrapper;
        this.channelConfig = channelConfig;

        this.sendbirdChatWrapper.addChannelHandler(ID_CHANNEL_EVENT_HANDLER, new GroupChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel channel, @NonNull BaseMessage message) {
                if (ChannelViewModel.this.getChannel() != null && channel.getUrl().equals(channelUrl) && hasNext()) {
                    markAsRead();
                    notifyDataSetChanged(new MessageContext(CollectionEventSource.EVENT_MESSAGE_RECEIVED, SendingStatus.SUCCEEDED));
                }
            }
        });

        this.sendbirdChatWrapper.addConnectionHandler(CONNECTION_HANDLER_ID, new ConnectionHandler() {
            @Override
            public void onDisconnected(@NonNull String s) {
            }

            @Override
            public void onConnected(@NonNull String s) {
            }

            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                loadLatestMessagesForCache();
            }

            @Override
            public void onReconnectFailed() {
            }
        });
    }

    /**
     * Checks if the message with {@code messageId} is in the message list which this view model manages.
     *
     * @param messageId ID of the message you want to check
     * @return {@code true} if the message in in the message list, {@code false} otherwise
     * since 3.0.0
     */
    public boolean hasMessageById(long messageId) {
        return cachedMessages.getById(messageId) != null;
    }

    /**
     * Retrieves message that matches {@code messageId} from the message list which this view model manages.
     *
     * @param messageId ID of the message you want to retrieve
     * @return {@code BaseMessage} that matches {@code messageId}
     * since 3.3.0
     */
    @Nullable
    public BaseMessage getMessageById(long messageId) {
        return cachedMessages.getById(messageId);
    }

    /**
     * Retrieves messages created at {@code createdAt} from the message list which this view model manages.
     *
     * @param createdAt The timestamp messages were created
     * @return The list of messages created at {@code createdAt}
     * since 3.0.0
     */
    @NonNull
    public List<BaseMessage> getMessagesByCreatedAt(long createdAt) {
        return cachedMessages.getByCreatedAt(createdAt);
    }

    // Do not call loadInitial inside this function.
    private synchronized void initMessageCollection(final long startingPoint) {
        Logger.i(">> ChannelViewModel::initMessageCollection()");
        final GroupChannel channel = getChannel();
        if (channel == null) return;
        if (this.collection != null) {
            disposeMessageCollection();
        }
        if (this.messageListParams == null) {
            this.messageListParams = createMessageListParams();
        }
        this.messageListParams.setReverse(true);
        this.collection = createMessageCollection(startingPoint, this.messageListParams, channel, new MessageCollectionHandler() {
            @UiThread
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesAdded() from=%s", context.getCollectionEventSource());
                ChannelViewModel.this.onMessagesAdded(context, channel, messages);

                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesAdded(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesUpdated() from=%s", context.getCollectionEventSource());
                ChannelViewModel.this.onMessagesUpdated(context, channel, messages);

                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesUpdated(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChannelViewModel::onMessagesDeleted() from=%s", context.getCollectionEventSource());

                ChannelViewModel.this.onMessagesDeleted(context, channel, messages);
                notifyMessagesDeleted(messages);
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onMessagesDeleted(context, channel, messages);
                }
            }

            @UiThread
            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> ChannelViewModel::onChannelDeleted() from=%s", context.getCollectionEventSource());
                notifyChannelDeleted(channelUrl);
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onChannelDeleted(context, channelUrl);
                }
            }

            @UiThread
            @Override
            public void onHugeGapDetected() {
                Logger.d(">> ChannelViewModel::onHugeGapDetected()");
                notifyHugeGapDetected();
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onHugeGapDetected();
                }
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
                Logger.d(">> ChannelViewModel::onChannelUpdated() from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());

                switch (context.getCollectionEventSource()) {
                    case EVENT_TYPING_STATUS_UPDATED:
                        final List<User> typingUsers = channel.getTypingUsers();
                        if (typingUsers.size() > 0) {
                            typingMembers.setValue(typingUsers);
                        } else {
                            typingMembers.setValue(null);
                        }
                        break;
                    case EVENT_DELIVERY_STATUS_UPDATED:
                    case EVENT_READ_STATUS_UPDATED:
                        notifyDataSetChanged(context);
                        break;
                    default:
                        break;
                }

                notifyChannelDataChanged();
                if (ChannelViewModel.this.handler != null) {
                    ChannelViewModel.this.handler.onChannelUpdated(context, channel);
                }
            }
        });
        Logger.i(">> ChannelViewModel::initMessageCollection() collection=%s", collection);
        loadLatestMessagesForCache();
    }

    @Override
    void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
        super.onMessagesAdded(context, channel, messages);
        switch (context.getCollectionEventSource()) {
            case EVENT_MESSAGE_RECEIVED:
            case EVENT_MESSAGE_SENT:
            case MESSAGE_FILL:
                markAsRead();
                break;
        }
    }

    // If the collection starts with a starting point value, not MAX_VALUE,
    // the message should be requested the newest messages at once because there may be no new messages in the cache
    private void loadLatestMessagesForCache() {
        if (!needToLoadMessageCache || (this.collection != null && this.collection.getStartingPoint() == Long.MAX_VALUE))
            return;
        final GroupChannel channel = getChannel();
        if (channel == null) return;
        final MessageCollectionWrapper syncCollection = createSyncMessageCollection(channel);
        syncCollection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {}

            @Override
            public void onApiResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {
                if (e == null) {
                    needToLoadMessageCache = false;
                }
                syncCollection.dispose();
            }
        });
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> ChannelViewModel::disposeMessageCollection()");
        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }

    /**
     * Registers a handler for the message collection managed by this view model.
     *
     * @param handler {@link MessageCollectionHandler} to be registered in this view model
     * since 3.0.0
     */
    public void setMessageCollectionHandler(@Nullable MessageCollectionHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<GroupChannel> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if huge gaps are detected within the collection this view model managed.
     *
     * @return LiveData holding whether huge gaps are detected
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getHugeGapDetected() {
        return hugeGapDetected;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<String> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns LiveData that can be observed if the messages has been deleted in the collection this view model managed.
     *
     * @return LiveData holding the list of deleted messages
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<BaseMessage>> onMessagesDeleted() {
        return messagesDeleted;
    }

    /**
     * Returns parameters required to retrieve the message list from this view model
     *
     * @return {@link MessageListParams} used in this view model
     * since 3.0.0
     */
    @Nullable
    public MessageListParams getMessageListParams() {
        return messageListParams;
    }

    /**
     * Returns LiveData that can be observed for members who are typing in the channel associated with this view model.
     *
     * @return LiveData holding members who are typing
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<User>> getTypingMembers() {
        return typingMembers;
    }

    /**
     * Returns LiveData that can be observed for the state of loading messages.
     *
     * @return LiveData holding {@link MessageLoadState} for this view model
     * since 3.0.0
     */
    @NonNull
    public LiveData<MessageLoadState> getMessageLoadState() {
        return messageLoadState;
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the message list.
     * When the message list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the message list
     * since 3.0.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    @Override
    public boolean hasNext() {
        return collection == null || collection.getHasNext();
    }

    @Override
    public boolean hasPrevious() {
        return collection == null || collection.getHasPrevious();
    }

    /**
     * Returns the timestamp that is the starting point when the message list is fetched initially.
     *
     * @return The timestamp as the starting point
     * since 3.0.0
     */
    public long getStartingPoint() {
        return collection != null ? collection.getStartingPoint() : Long.MAX_VALUE;
    }

    @UiThread
    private synchronized void notifyChannelDataChanged() {
        Logger.d(">> ChannelViewModel::notifyChannelDataChanged()");
        final GroupChannel groupChannel = getChannel();
        if (groupChannel == null) return;
        channelUpdated.setValue(groupChannel);
    }

    @Override
    synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> ChannelViewModel::notifyDataSetChanged(), size = %s, action=%s, hasNext=%s", cachedMessages.size(), traceName, hasNext());
        if (collection == null) return;

        // even though a pending message is added, if the message is sent from the Thread page it shouldn't scroll to the first.
        final List<BaseMessage> pendingMessages = new ArrayList<>(collection.getPendingMessages());
        final List<BaseMessage> failedMessages = new ArrayList<>(collection.getFailedMessages());
        if (channelConfig.getReplyType() == ReplyType.THREAD) {
            boolean shouldCheckEvents = traceName.equals(StringSet.ACTION_FAILED_MESSAGE_ADDED)
                || traceName.equals(StringSet.ACTION_PENDING_MESSAGE_ADDED);

            final BaseMessage lastPendingMessage = !pendingMessages.isEmpty() ? pendingMessages.get(0) : null;
            final BaseMessage lastFailedMessage = !failedMessages.isEmpty() ? failedMessages.get(0) : null;
            final long lastPendingMessageCreatedAt = lastPendingMessage != null ? lastPendingMessage.getCreatedAt() : 0L;
            final long lastFailedMessageCreatedAt = lastFailedMessage != null ? lastFailedMessage.getCreatedAt() : 0L;

            boolean isThreadMessage = false;
            if (lastPendingMessageCreatedAt > lastFailedMessageCreatedAt) {
                isThreadMessage = lastPendingMessage != null && lastPendingMessage.isReplyToChannel();
            } else if (lastPendingMessageCreatedAt < lastFailedMessageCreatedAt) {
                isThreadMessage = lastFailedMessage != null && lastFailedMessage.isReplyToChannel();
            }

            if (shouldCheckEvents && isThreadMessage) return;

            // even though it is not a pending message event, it has to remove pending message from the thread page for the other events.
            removeThreadMessages(pendingMessages);
            removeThreadMessages(failedMessages);
        }

        final List<BaseMessage> copiedList = cachedMessages.toList();
        if (!hasNext()) {
            copiedList.addAll(0, pendingMessages);
            copiedList.addAll(0, failedMessages);

            SuggestedRepliesMessage suggestedRepliesMessage = createSuggestedRepliesMessage();
            if (suggestedRepliesMessage != null) {
                copiedList.add(0, suggestedRepliesMessage);
            }
        }

        if (copiedList.size() == 0) {
            statusFrame.setValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.setValue(StatusFrameView.Status.NONE);
        }

        messageList.setValue(new ChannelMessageData(traceName, copiedList));
    }

    private void removeThreadMessages(@NonNull List<BaseMessage> src) {
        final ListIterator<BaseMessage> iterator = src.listIterator();
        while (iterator.hasNext()) {
            if (MessageUtils.hasParentMessage(iterator.next())) {
                iterator.remove();
            }
        }
    }

    @Nullable
    private SuggestedRepliesMessage createSuggestedRepliesMessage() {
        if (!channelConfig.getEnableSuggestedReplies()) return null;
        if (hasNext()) return null;

        if (collection != null) {
            List<BaseMessage> pendingMessages = collection.getPendingMessages();
            List<BaseMessage> failedMessages = collection.getFailedMessages();
            if (!pendingMessages.isEmpty() || !failedMessages.isEmpty()) return null;
        }

        GroupChannel groupChannel = channel;
        if (groupChannel != null) {
            BaseMessage lastMessage = groupChannel.getLastMessage();
            if (lastMessage != null && !MessageExtensionsKt.getSuggestedReplies(lastMessage).isEmpty()) {
                return new SuggestedRepliesMessage(lastMessage);
            }
        }

        return null;
    }

    public void markAsRead() {
        if (!isChatScreenVisible) return;

        Logger.dev("markAsRead");
        if (channel != null) channel.markAsRead(null);
    }

    public void setIsChatScreenVisible(boolean visible) {
        Logger.dev("setIsChatScreenVisible: " + visible);
        isChatScreenVisible = visible;
    }

    @UiThread
    private synchronized void notifyChannelDeleted(@NonNull String channelUrl) {
        channelDeleted.setValue(channelUrl);
    }

    @UiThread
    private synchronized void notifyMessagesDeleted(@NonNull List<BaseMessage> deletedMessages) {
        messagesDeleted.setValue(deletedMessages);
    }

    @UiThread
    private synchronized void notifyHugeGapDetected() {
        hugeGapDetected.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        this.sendbirdChatWrapper.removeChannelHandler(ID_CHANNEL_EVENT_HANDLER);
        this.sendbirdChatWrapper.removeConnectionHandler(CONNECTION_HANDLER_ID);
        disposeMessageCollection();
    }

    @Override
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        super.deleteMessage(message, handler);
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.FAILED) {
            if (collection != null) {
                collection.removeFailedMessages(Collections.singletonList(message), (requestIds, e) -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ deleted message : %s", message);
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
                    if (message instanceof FileMessage) {
                        PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
                    }
                });
            }
        }
    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @param startingPoint Timestamp that is the starting point when the message list is fetched
     * since 3.0.0
     */
    @UiThread
    public synchronized boolean loadInitial(final long startingPoint) {
        Logger.d(">> ChannelViewModel::loadInitial() startingPoint=%s", startingPoint);
        initMessageCollection(startingPoint);
        if (collection == null) {
            Logger.d("-- channel instance is null. an authenticate process must be proceed first");
            return false;
        }

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        cachedMessages.clear();
        collection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> cachedList, @Nullable SendbirdException e) {
                if (e == null && cachedList != null && cachedList.size() > 0) {
                    cachedMessages.addAll(cachedList);
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_CACHE);
                }
            }

            @Override
            public void onApiResult(@Nullable List<BaseMessage> apiResultList, @Nullable SendbirdException e) {
                if (e == null && apiResultList != null) {
                    cachedMessages.clear();
                    cachedMessages.addAll(apiResultList);
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                    if (apiResultList.size() > 0) {
                        markAsRead();
                    }
                }
                messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
            }
        });
        return true;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious() || collection == null) return Collections.emptyList();
        Logger.i(">> ChannelViewModel::loadPrevious()");

        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        collection.loadPrevious((messages, e) -> {
            Logger.d("++ privious size = %s", messages == null ? 0 : messages.size());
            try {
                if (e == null) {
                    if (messages != null) {
                        cachedMessages.addAll(messages);
                    }
                    result.set(messages);
                    notifyDataSetChanged(StringSet.ACTION_PREVIOUS);
                }
                error.set(e);
            } finally {
                lock.countDown();
            }
        });
        lock.await();

        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the next.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadNext() throws Exception {
        if (!hasNext() || collection == null) return Collections.emptyList();

        Logger.i(">> ChannelViewModel::loadNext()");
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        collection.loadNext((messages, e) -> {
            try {
                if (e == null) {
                    messages = messages == null ? Collections.emptyList() : messages;
                    cachedMessages.addAll(messages);
                    result.set(messages);
                    notifyDataSetChanged(StringSet.ACTION_NEXT);
                }
                error.set(e);
            } finally {
                lock.countDown();
            }
        });
        lock.await();

        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Creates params for the message list when loading the message list.
     *
     * @return {@link MessageListParams} to be used when loading the message list
     * since 3.0.0
     */
    @NonNull
    public MessageListParams createMessageListParams() {
        final MessageListParams messageListParams = new MessageListParams();
        messageListParams.setReverse(true);
        if (channelConfig.getReplyType() != ReplyType.NONE) {
            messageListParams.setReplyType(com.sendbird.android.message.ReplyType.ONLY_REPLY_TO_CHANNEL);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), true, true));
        } else {
            messageListParams.setReplyType(com.sendbird.android.message.ReplyType.NONE);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), false, true));
        }
        return messageListParams;
    }

    @VisibleForTesting
    @NonNull
    MessageCollectionWrapper createMessageCollection(long startingPoint, @NonNull MessageListParams params, @NonNull GroupChannel channel, @NonNull MessageCollectionHandler handler) {
        return new MessageCollectionImpl(SendbirdChat.createMessageCollection(new MessageCollectionCreateParams(channel, params, startingPoint, handler)));
    }

    @VisibleForTesting
    @NonNull
    MessageCollectionWrapper createSyncMessageCollection(GroupChannel channel) {
        return new MessageCollectionImpl(SendbirdChat.createMessageCollection(new MessageCollectionCreateParams(channel, new MessageListParams())));
    }

    @TestOnly
    @NonNull
    String getChannelHandlerIdentifier() {
        return ID_CHANNEL_EVENT_HANDLER;
    }

    @TestOnly
    @NonNull
    String getConnectionHandlerIdentifier() {
        return CONNECTION_HANDLER_ID;
    }

    @TestOnly
    boolean isNeedToLoadMessageCache() {
        return needToLoadMessageCache;
    }

    @TestOnly
    @Nullable
    MessageCollectionWrapper getCollection() {
        return collection;
    }
}
