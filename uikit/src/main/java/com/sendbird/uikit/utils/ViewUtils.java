package com.sendbird.uikit.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.CustomizableMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.OGMetaData;
import com.sendbird.android.message.Thumbnail;
import com.sendbird.android.user.Sender;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.internal.model.GlideCachedUrlLoader;
import com.sendbird.uikit.internal.ui.messages.BaseQuotedMessageView;
import com.sendbird.uikit.internal.ui.messages.OgtagView;
import com.sendbird.uikit.internal.ui.messages.ThreadInfoView;
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionListView;
import com.sendbird.uikit.internal.ui.widgets.RoundCornerView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.MentionSpan;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.vm.PendingMessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The helper class for the drawing views in the UIKit.
 * It is used to draw common UI from each custom component.
 */
public class ViewUtils {
    private final static int MINIMUM_THUMBNAIL_WIDTH = 100;
    private final static int MINIMUM_THUMBNAIL_HEIGHT = 100;
    public static final Pattern MENTION = Pattern.compile("[" + SendbirdUIKit.getUserMentionConfig().getTrigger() + "][{](.*?)([}])");

    private static void drawUnknownMessage(@NonNull TextView view, boolean isMine) {
        int unknownHintAppearance;
        if (isMine) {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnLight02 : R.style.SendbirdBody3OnDark02;
        } else {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnDark03 : R.style.SendbirdBody3OnLight02;
        }

        final int sizeOfFirstLine = 23;
        String unknownHintText = view.getContext().getResources().getString(R.string.sb_text_channel_unknown_type_text);
        final Spannable spannable = new SpannableString(unknownHintText);
        spannable.setSpan(new TextAppearanceSpan(view.getContext(), unknownHintAppearance), sizeOfFirstLine, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

    public static void drawTextMessage(@NonNull TextView textView, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        drawTextMessage(textView, message, uiConfig, null);
    }

    public static void drawTextMessage(@NonNull TextView textView, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig) {
        if (message == null) {
            return;
        }

        if (MessageUtils.isUnknownType(message)) {
            drawUnknownMessage(textView, MessageUtils.isMine(message));
            return;
        }

        final boolean isMine = MessageUtils.isMine(message);
        final Context context = textView.getContext();
        final CharSequence text = getDisplayableText(context, message, uiConfig, mentionedCurrentUserUIConfig, true);
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        if (message.getUpdatedAt() > 0L) {
            final String edited = textView.getResources().getString(R.string.sb_text_channel_message_badge_edited);
            final Spannable editedString = new SpannableString(edited);
            if (uiConfig != null) {
                final TextUIConfig editedTextMarkUIConfig = isMine ? uiConfig.getMyEditedTextMarkUIConfig() : uiConfig.getOtherEditedTextMarkUIConfig();
                editedTextMarkUIConfig.bind(context, editedString, 0, editedString.length());
            }
            builder.append(editedString);
        }

        textView.setText(builder);
    }

    @NonNull
    public static CharSequence getDisplayableText(@NonNull Context context, @NonNull BaseMessage message, @Nullable MessageUIConfig uiConfig, @Nullable TextUIConfig mentionedCurrentUserUIConfig, boolean mentionClickable) {
        final String mentionedText = message.getMentionedMessageTemplate();
        final SpannableString text = new SpannableString(message.getMessage());
        if (uiConfig != null) {
            final TextUIConfig messageTextUIConfig = MessageUtils.isMine(message) ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            messageTextUIConfig.bind(context, text, 0, text.length());
        }

        CharSequence displayText = text;
        if (SendbirdUIKit.isUsingUserMention() && !message.getMentionedUsers().isEmpty() && !TextUtils.isEmpty(mentionedText)) {
            final SpannableString mentionedSpannableString = new SpannableString(mentionedText);
            if (uiConfig != null) {
                final TextUIConfig messageTextUIConfig = MessageUtils.isMine(message) ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
                messageTextUIConfig.bind(context, mentionedSpannableString, 0, mentionedSpannableString.length());
            }
            final Matcher matcher = MENTION.matcher(mentionedSpannableString);
            final List<String> sources = new ArrayList<>();
            final List<CharSequence> destinations = new ArrayList<>();
            while (matcher.find()) {
                if (matcher.groupCount() < 2) break;
                Logger.d("_____ matched group[0] = %s, group[1] = %s, start=%d, end=%d, count=%d", matcher.group(0), matcher.group(1), matcher.start(), matcher.end(), matcher.groupCount());

                final String mentionedUserId = matcher.group(1);
                if (mentionedUserId != null) {
                    final User mentionedUser = getMentionedUser(message, mentionedUserId);
                    if (mentionedUser != null) {
                        final boolean isMine = MessageUtils.isMine(message);
                        final boolean isMentionedCurrentUser = MessageUtils.isMine(mentionedUserId);
                        final String trigger = SendbirdUIKit.getUserMentionConfig().getTrigger();
                        final SpannableString spannable;
                        if (uiConfig != null) {
                            final TextUIConfig config = isMine ? uiConfig.getMyMentionUIConfig() : uiConfig.getOtherMentionUIConfig();
                            final String nickname = UserUtils.getDisplayName(context, mentionedUser);
                            final MentionSpan mentionSpan = new MentionSpan(context, trigger, nickname, mentionedUser, config, isMentionedCurrentUser ? mentionedCurrentUserUIConfig : null);
                            spannable = new SpannableString(mentionSpan.getDisplayText());
                            spannable.setSpan(mentionSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spannable = new SpannableString(trigger + mentionedUser.getNickname());
                        }
                        if (mentionClickable) {
                            spannable.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    SoftInputUtils.hideSoftKeyboard(widget);
                                    DialogUtils.showUserProfileDialog(context, mentionedUser, !isMentionedCurrentUser, null, null);
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint paint) {
                                    paint.setUnderlineText(false);
                                }
                            }, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        destinations.add(spannable);
                        sources.add(matcher.group(0));
                    }
                }
            }
            int arraySize = sources.size();
            displayText = TextUtils.replace(mentionedSpannableString, sources.toArray(new String[arraySize]), destinations.toArray(new CharSequence[arraySize]));
        }
        return displayText;
    }

    @Nullable
    private static User getMentionedUser(@NonNull BaseMessage message, @NonNull String targetUserId) {
        final List<User> mentionedUserList = message.getMentionedUsers();
        for (User user : mentionedUserList) {
            if (user.getUserId().equals(targetUserId)) {
                return user;
            }
        }
        return null;
    }

    public static void drawOgtag(@NonNull ViewGroup parent, @Nullable OGMetaData ogMetaData) {
        if (ogMetaData == null) {
            return;
        }

        parent.removeAllViews();
        OgtagView ogtagView = OgtagView.inflate(parent.getContext(), parent);
        ogtagView.drawOgtag(ogMetaData);
        parent.setOnClickListener(v -> {
            if (ogMetaData.getUrl() == null) {
                return;
            }

            Intent intent = IntentUtils.getWebViewerIntent(ogMetaData.getUrl());
            try {
                ogtagView.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Logger.e(e);
            }
        });
    }

    public static void drawReactionEnabled(@NonNull EmojiReactionListView view, @NonNull BaseChannel channel) {
        boolean canSendReaction = ReactionUtils.canSendReaction(channel);
        view.setClickable(canSendReaction);
        if (view.useMoreButton() != canSendReaction) {
            view.setUseMoreButton(canSendReaction);
            view.refresh();
        }
    }

    public static void drawNickname(@NonNull TextView tvNickname, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig, boolean isOperator) {
        if (message == null) {
            return;
        }

        final Sender sender = message.getSender();
        final Spannable nickname = new SpannableString(UserUtils.getDisplayName(tvNickname.getContext(), sender));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isOperator ? uiConfig.getOperatorNicknameTextUIConfig() :
                    (isMine ? uiConfig.getMyNicknameTextUIConfig() : uiConfig.getOtherNicknameTextUIConfig());
            textUIConfig.bind(tvNickname.getContext(), nickname, 0, nickname.length());
        }

        tvNickname.setText(nickname);
    }

    public static void drawNotificationProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = R.color.background_300;
        int inset = ivProfile.getContext().getResources().getDimensionPixelSize(R.dimen.sb_size_6);
        final Drawable profile = DrawableUtils.createOvalIconWithInset(ivProfile.getContext(),
                backgroundTint, R.drawable.icon_channels, iconTint, inset);
        ivProfile.setImageDrawable(profile);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        if (message == null) {
            return;
        }
        Sender sender = message.getSender();

        String url = "";
        String plainUrl = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
            plainUrl = sender.getPlainProfileImageUrl();
        }

        drawProfile(ivProfile, url, plainUrl);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable String url, @Nullable String plainUrl) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = R.color.background_300;
        Drawable errorDrawable = DrawableUtils.createOvalIcon(ivProfile.getContext(),
                backgroundTint, R.drawable.icon_user, iconTint);

        if (url == null || plainUrl == null) return;
        GlideCachedUrlLoader.load(Glide.with(ivProfile.getContext()), url, String.valueOf(plainUrl.hashCode()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(errorDrawable)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfile);
    }

    public static void drawThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message) {
        drawThumbnail(view, message, null, R.dimen.sb_size_48);
    }

    public static void drawQuotedMessageThumbnail(@NonNull RoundCornerView view,
                                                  @NonNull FileMessage message,
                                                  @Nullable RequestListener<Drawable> requestListener) {
        drawThumbnail(view, message, requestListener, R.dimen.sb_size_24);
    }

    private static void drawThumbnail(@NonNull RoundCornerView view,
                                      @NonNull FileMessage message,
                                      @Nullable RequestListener<Drawable> requestListener,
                                      @DimenRes int iconSize
                                      ) {
        String url = message.getUrl();
        if (TextUtils.isEmpty(url) && message.getMessageCreateParams() != null &&
                message.getMessageCreateParams().getFile() != null) {
            url = message.getMessageCreateParams().getFile().getAbsolutePath();
        }
        Context context = view.getContext();
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        RequestBuilder<Drawable> builder = Glide.with(context)
                .asDrawable()
                .apply(options);

        Pair<Integer, Integer> defaultResizingSize = SendbirdUIKit.getResizingSize();
        int width = defaultResizingSize.first / 2;
        int height = defaultResizingSize.second / 2;
        FileInfo fileInfo = PendingMessageRepository.getInstance().getFileInfo(message);
        if (fileInfo != null) {
            width = fileInfo.getThumbnailWidth();
            height = fileInfo.getThumbnailHeight();
            builder = builder.override(width, height);
            if (!TextUtils.isEmpty(fileInfo.getThumbnailPath())) {
                url = fileInfo.getThumbnailPath();
            }
        } else {
            List<Thumbnail> thumbnails = message.getThumbnails();
            Thumbnail thumbnail = null;
            if (thumbnails.size() > 0) {
                thumbnail = thumbnails.get(0);
            }
            if (thumbnail != null && !TextUtils.isEmpty(thumbnail.getUrl())) {
                Logger.dev("++ thumbnail width : %s, thumbnail height : %s", thumbnail.getRealWidth(), thumbnail.getRealHeight());
                width = Math.max(MINIMUM_THUMBNAIL_WIDTH, thumbnail.getRealWidth());
                height = Math.max(MINIMUM_THUMBNAIL_HEIGHT, thumbnail.getRealHeight());
                url = thumbnail.getUrl();
                builder = builder.override(width, height);
            } else {
                final int size = Math.min(Math.max(MINIMUM_THUMBNAIL_WIDTH, width), Math.max(MINIMUM_THUMBNAIL_HEIGHT, height));
                builder = builder.override(size);
            }
        }

        if (message.getType().toLowerCase().contains(StringSet.image) && !message.getType().toLowerCase().contains(StringSet.gif)) {
            view.getContent().setScaleType(ImageView.ScaleType.CENTER);
            int thumbnailIconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
            builder = builder
                    .placeholder(DrawableUtils.setTintList(
                            ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_photo), iconSize, iconSize),
                            AppCompatResources.getColorStateList(context, thumbnailIconTint)))
                    .error(DrawableUtils.setTintList(
                            ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_thumbnail_none), iconSize, iconSize),
                            AppCompatResources.getColorStateList(context, thumbnailIconTint)));
        }

        final String cacheKey = generateThumbnailCacheKey(message);
        GlideCachedUrlLoader.load(builder, url, cacheKey)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (requestListener != null) {
                            requestListener.onLoadFailed(e, model, target, isFirstResource);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        view.getContent().setScaleType(ImageView.ScaleType.CENTER_CROP);
                        if (requestListener != null) {
                            requestListener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                        }
                        return false;
                    }
                })
                .into(view.getContent());
    }

    private static String generateThumbnailCacheKey(@NonNull FileMessage message) {
        final String requestId = message.getRequestId();
        if (TextUtils.isNotEmpty(requestId)) {
            return "thumbnail_" + message.getRequestId();
        }
        return String.valueOf(message.getPlainUrl().hashCode());
    }

    public static void drawThumbnailIcon(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        String type = fileMessage.getType();
        Context context = imageView.getContext();
        int backgroundTint = R.color.ondark_01;
        int iconTint = R.color.onlight_02;
        if (type.toLowerCase().contains(StringSet.gif)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_gif, iconTint));
        } else if (type.toLowerCase().contains(StringSet.video)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_play, iconTint));
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }
    }

    public static void drawFileIcon(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50;
        int iconTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_4);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);
        if ((fileMessage.getType().toLowerCase().startsWith(StringSet.audio))) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawFileMessageIconToReply(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        String type = fileMessage.getType();
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_500 : R.color.background_100;
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_8);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);

        if ((fileMessage.getType().toLowerCase().startsWith(StringSet.audio))) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else if ((type.startsWith(StringSet.image) && !type.contains(StringSet.svg)) ||
                type.toLowerCase().contains(StringSet.gif) ||
                type.toLowerCase().contains(StringSet.video)) {
            imageView.setImageResource(android.R.color.transparent);
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawQuotedMessage(@NonNull BaseQuotedMessageView replyPanel, @NonNull GroupChannel channel, @NonNull BaseMessage message, @Nullable TextUIConfig uiConfig) {
        final boolean hasParentMessage = MessageUtils.hasParentMessage(message);
        replyPanel.setVisibility(hasParentMessage ? View.VISIBLE : View.GONE);
        replyPanel.drawQuotedMessage(channel, message, uiConfig);
    }

    public static void drawSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Spannable sentAt = new SpannableString(DateUtils.formatTime(tvSentAt.getContext(), message.getCreatedAt()));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(tvSentAt.getContext(), sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawParentMessageSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Context context = tvSentAt.getContext();
        final long createdAt = message.getCreatedAt();
        final String sentAtTime = DateUtils.formatTime(context, createdAt);
        final String sentAtDate = DateUtils.isThisYear(createdAt) ? DateUtils.formatDate2(createdAt) : DateUtils.formatDate4(createdAt);
        final Spannable sentAt = new SpannableString(sentAtDate + " " + sentAtTime);
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(context, sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @Nullable FileMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Spannable filename = new SpannableString(message.getName());
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            textUIConfig.bind(tvFilename.getContext(), filename, 0, filename.length());
        }

        tvFilename.setText(filename);
    }

    public static void drawThreadInfo(@NonNull ThreadInfoView threadInfoView, @NonNull BaseMessage message) {
        if (message instanceof CustomizableMessage) return;
        if (SendbirdUIKit.getReplyType() != ReplyType.THREAD) {
            threadInfoView.setVisibility(View.GONE);
            return;
        }
        threadInfoView.drawThreadInfo(message.getThreadInfo());
    }
}
