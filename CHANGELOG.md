# Changelog

### v3.5.0.beta.0 (Feb 6, 2023) with Chat SDK `v4.2.1`
* Support Notification Channel
A notification channel is a new group channel dedicated to receiving one way marketing and transactional messages.To allow users to view messages sent through Sendbird Message Builder with the correct rendering, you need to implement the notification channel view using `NotificationChannelActivity`, `NotificationChannelViewModel`, or `NotificationChannelFragment`
  * Added `NotificationChannelActivity`, `NotificationChannelViewModel`, and `NotificationChannelFragment`
  * Added `Action` class to pass data about events
  * Added Notification module
    * Added `NotificationChannelModule`, `NotificationMessageListComponent`, and `NotificationMessageListAdapter`
  * Added `newNotificationChannelFragment(String, Bundle)` in `UIKitFragmentFactory`
  * Added new style sets
    * `Widget.Sendbird.Message.NotificationChannel` and `Widget.Sendbird.Dark.Message.NotificationChannel`
    * `Widget.Sendbird.Message.MessageTemplateView` and `Widget.Sendbird.Dark.Message.MessageTemplateView`
    * `Module.NotificationChannel` and `Module.Dark.NotificationChannel`
    * `Component.Header.NotificationChannel` and `Component.Dark.Header.NotificationChannel`
    * `Component.List.NotificationChannel` and `Component.Dark.List.NotificationChannel`
    * `Component.Status.NotificationChannel` and `Component.Dark.Status.NotificationChannel`

### v3.3.3 (Jan 19, 2023) with Chat SDK `v4.2.1`
* Improved stability

### v3.3.2 (Dec 09, 2022) with Chat SDK `v4.1.3`
* Support authenticated file caching
* Change the default value of `SendbirdUIKit.shouldUseImageCompression()` to `true`
* Change the default value of `SendbirdUIKit.getCompressQuality()` to `70`
* Improved message input dialog mode
* Improved stability

### v3.3.1 (Nov 21, 2022) with Chat SDK `v4.1.3`
* Fixed message update issue when an app is built with Proguard on
* Improved stability

### v3.3.0 (Nov 10, 2022) with Chat SDK `v4.1.1`
* Support thread type in GroupChannel
  * Added `THREAD` in `ReplyType`
  * Added `enum ThreadReplySelectType { PARENT, THREAD }`
  * Added `setThreadReplySelectType(threadReplySelectType)` in `SendBirdUIKit`
  * Added `getThreadReplySelectType()` in `SendBirdUIKit`
  * Added `MessageThreadActivity`, `MessageThreadFragment`, `MessageThreadModule`, `MessageThreadViewModel`, `MessageThreadHeaderComponent`, `ThreadListComponent`, `MessageThreadInputComponent`, and `ThreadListAdapter`
  * Added `newRedirectToMessageThreadIntent(Context, String, long)` in `ChannelActivity`
  * Added `VIEW_TYPE_PARENT_MESSAGE_INFO` in `MessageType`
  * Added `ThreadInfo`, `ParentMessageMenu` in `ClickableViewIdentifier`
  * Added `onThreadInfoClicked(View, int, BaseMessage)` in `ChannelFragment`
  * Added `setOnThreadInfoClickListener(OnItemClickListener<BaseMessage>)` in `ChannelFragment.Builder`
* Added `MessageListUIParams` class
* Added `bind(BaseChannel, BaseMessage, MessageListUIParams)` in `MessageViewHolder`
* Added `createViewHolder(LayoutInflater, ViewGroup, MessageType, MessageListUIParams)` in `MessageViewHolderFactory`
* Added `createOpenChannelViewHolder(LayoutInflater, ViewGroup, MessageType, MessageListUIParams)` in `MessageViewHolderFactory`
* Deprecated `bind(BaseChannel, BaseMessage, MessageGroupType)` in `MessageViewHolder`
* Deprecated `createViewHolder(LayoutInflater, ViewGroup, MessageType, boolean)` in `MessageViewHolderFactory`
* Deprecated `createOpenChannelViewHolder(LayoutInflater, ViewGroup, MessageType, boolean)` in `MessageViewHolderFactory`
* Added `setUseMessageListBanner(boolean)` in `ChannelFragment.Builder`
* Added `setUseBanner(boolean)` in `MessageListComponent.Params`
* Added `setUseUserIdForNickname(boolean)` and `isUsingUserIdForNickname()` in `SendbirdUIKit`

### v3.2.2 (Oct 27, 2022) with Chat SDK `v4.1.1` 
* Added `setOnScrollFirstButtonClickListener(OnConsumableClickListener)` in `ChannelFragment.Builder` and `OpenChannelFragment.Builder`
* Added `scrollToFirst()`, `setOnScrollFirstButtonClickListener(OnConsumableClickListener)`, and `onScrollFirstButtonClicked(View)` in `MessageListComponent` and `OpemChannelMessageListComponent`
* Deprecated `setOnScrollBottomButtonClickListener(View.OnClickListener)` in `ChannelFragment.Builder` and `OpenChannelFragment.Builder`
* Deprecated `scrollToBottom()`, `setOnScrollBottomButtonClickListener(View.OnClickListener)`, and `onScrollBottomButtonClicked(View)` in `MessageListComponent` and `OpemChannelMessageListComponent`
* Improved stability

### v3.2.1 (Sep 29, 2022) with Chat SDK `v4.0.9`
* Added `takeVideo()` in `ChannelFragment` and `OpenChannelFragment`
* Support custom font in message bubble and input filed.
  * Added `setRepliedMessageTextUIConfig(TextUIConfig)`, and `setMessageInputTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` in `OpenChannelFragment.Builder`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` and `getMessageInputTextUIConfig()` in `MessageInputComponent.Params`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` and `getMessageInputTextUIConfig()` in `OpenChannelMessageInputComponent.Params`
  * Added `setRepliedMessageTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
  * Added `setCustomFontRes(int)` in `TextUIConfig.Builder`

### v3.2.0 (Sep 15, 2022) with Chat SDK `v4.0.8`
* Support OpenChannel list
  * Added `OpenChannelListActivity`, `OpenChannelListFragment`, `OpenChannelListModule`, `OpenChannelListViewModel`, `OpenChannelListComponent`, and `OpenChannelListAdapter`
  * Added `CreateOpenChannelActivity`, `CreateOpenChannelFragment`, `CreateOpenChannelModule`, `CreateOpenChannelViewModel`, and `ChannelProfileInputComponent`
* Moved widgets class into internal package.
* `setCustomFragment()` functions have been added in the all Fragment.Builder class
* Improved stability

### v3.1.1 (Aug 17, 2022) with Chat SDK `v4.0.5`
* Added `setMessageTextUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setSentAtTextUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setNicknameTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`, `MessageListComponent.Params`
* Added `setNicknameTextUIConfig(TextUIConfig, TextUIConfig, TextUIConfig)` in `OpenChannelFragment.Builder`, `OpenChannelMessageListComponent.Params`
* Added `setMessageBackground(int, int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setMessageBackground(Drawable, Drawable)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setReactionListBackground(int, int)` in `ChannelFragment.Builder`
* Added `setReactionListBackground(Drawable, Drawable)` in `MessageListComponent.Params`
* Added `setOgtagBackground(int, int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setOgtagBackground(Drawable, Drawable)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setLinkedTextColor(int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setLinkedTextColor(ColorStateList)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`

### v3.1.0 (Aug 3, 2022) with Chat SDK `v4.0.5`
* Support Android 13
  * Set the `maxSdkVersion` of `android.permission.READ_EXTERNAL_STORAGE` to `32`
* Removed `android.permission.REQUEST_INSTALL_PACKAGES` permission
* Support moderation in OpenChannel
  * Added `MODERATIONS` in `OpenChannelSettingsMenuComponent.Menu`
  * Added `OpenChannelModerationActivity`, `OpenChannelModerationFragment`, `OpenChannelModerationModule`, `OpenChannelModerationViewModel`
  * Added `OpenChannelOperatorListActivity`, `OpenChannelOperatorListFragment`, `OpenChannelOperatorListModule`, `OpenChannelOperatorListViewModel`, `OpenChannelOperatorListAdapter`
  * Added `OpenChannelRegisterOperatorActivity`, `OpenChannelRegisterOperatorFragment`, `OpenChannelRegisterOperatorModule`, `OpenChannelRegisterOperatorViewModel`, `OpenChannelRegisterOperatorAdapter`
  * Added `OpenChannelBannedUserListActivity`, `OpenChannelBannedUserListFragment`, `OpenChannelBannedUserListModule`, `OpenChannelBannedUserListViewModel`, `OpenChannelBannedUserListAdapter`
  * Added `OpenChannelMutedParticipantListActivity`, `OpenChannelMutedParticipantListFragment`, `OpenChannelMutedParticipantListModule`, `OpenChannelMutedParticipantListViewModel`, `OpenChannelMutedParticipantListAdapter`
  * Added `newOpenChannelModerationFragment()`, `newOpenChannelOperatorListFragment()`, `newOpenChannelRegisterOperatorFragment()`, `newOpenChannelMutedParticipantListFragment()`, `newOpenChannelBannedUserListFragment()` in `UIKitFragmentFactory`
* Improved stability

### v3.0.0 (Jul 12, 2022) with Chat SDK `v4.0.4`
* Support `modules` and `components` in the UIKit
* Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `OpenChannelFragment.Builder`
* Rename `PromoteOperator` to `RegisterOperator`
  * Rename `PromoteOperatorActivity` to `RegisterOperatorActivity`
  * Rename `PromoteOperatorFragment` to `RegisterOperatorFragment`
  * Rename `PromoteOperatorModule` to `RegisterOperatorModule`
  * Rename `PromoteOperatorListComponent` to `RegisterOperatorListComponent`
  * Rename `PromoteOperatorListAdapter` to `RegisterOperatorListAdapter`
  * Rename `PromoteOperatorViewModel` to `RegisterOperatorViewModel`
  * Rename `newPromoteOperatorFragment()` to `newRegisterOperatorFragment()` in `UIKitFragmentFactory`
  * Rename `onBindPromoteOperatorListComponent()` to `onBindRegisterOperatorListComponent()` in `RegisterOperatorFragment`
  * Rename `setPromoteOperatorListAdapter()` to `setRegisterOperatorListAdapter()` in `RegisterOperatorFragment.Builder`
  * Rename `setMemberListComponent()` to `setRegisterOperatorListComponent()` in `RegisterOperatorModule`
  * Rename `getPromoteOperatorListComponent()` to `getRegisterOperatorListComponent()` in `RegisterOperatorModule`
  * Rename `getOperatorDismissed()` to `getOperatorUnregistered()` in `PromoteOperatorViewModel`
* See more details and breaking changes. [[details](/changelogs/BREAKINGCHANGES_V3.md)]
* See the Migration Guide for Converting V2 to V3. [[details](/changelogs/MIGRATIONGUIDE_V3.md)]

### v3.0.0-beta.3 (Jun 02, 2022) with Chat SDK `v3.1.14`
* Synchronized Builder methods and Params methods
  * Added `setErrorText()` in `BannedUserListFragment.Builder`, `ChannelListFragment.Builder`, `MemberListFragment.Builder`, `MutedMemberListFragment.Builder`, `OperatorListFragment.Builder`
  * Added `setOnInputRightButtonClickListener()`, `setOnEditModeCancelButtonClickListener()`, `setOnEditModeSaveButtonClickListener()`, `setOnQuoteReplyModeCloseButtonClickListener()`, `setOnInputModeChangedListener()`, `setUseSuggestedMentionListDivider()`, `setOnTooltipClickListener()`, `setOnScrollBottomButtonClickListener()`, `setErrorText()` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
  * Added `setRightButtonText()`, `setOnRightButtonClickListener()` in `ChannelSettings.Builder`
  * Added `setOnRightButtonClickListener()`, `setOnUserSelectChangedListener()`, `setOnUserSelectionCompleteListener()`, `setErrorText()` in `CreateChannelFragment.Builder`, `PromoteOperatorFragment.Builder`
  * Added `setOnInputTextChangedListener()`, `setOnClearButtonClickListener()`, `setErrorText()` in `MessageSearchFragment.Builder`
  * Added `setHeaderRightButtonIconResId()`, `setHeaderRightButtonIcon()`, `setUseHeaderRightButton()`, `setOnHeaderRightButtonClickListener()` in `ModerationFragment.Builder`
  * Added `setOnInputRightButtonClickListener()`, `setOnEditModeCancelButtonClickListener()`, `setOnEditModeSaveButtonClickListener()`, `setOnInputModeChangedListener()`, `setOnScrollBottomButtonClickListener()`, `setOnMessageProfileLongClickListener()`, `setOnMessageInsertedListener()`, `setErrorText()` in `OpenChannelFragment.Builder`
  * Added `setHeaderRightButtonIconResId()`, `setHeaderRightButtonIcon()`, `setUseHeaderRightButton()`, `setOnHeaderRightButtonClickListener()`, `setOnActionItemClickListener()`, `setErrorText()` in `ParticipantListFragment.Builder`

* Mention improvement
  * Added `setStartingPoint(long, boolean)` in `ChannelFragment.Builder`
  * Removed `HighlightMessageInfo` class
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)` in `ChannelFragment.Builder`
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)` and `getHighlightMessageInfo()` in `MessageListAdapter`
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)`, `getHighlightMessageInfo()`, `setSearchedTextUIConfig(TextUIConfig)`, and `setSearchedTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
  * Removed `setSearchedTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`

* Added channel push setting option for sent from mentioned only
* Added `ChannelPushSettingFragment` and `ChannelPushSettingActivity`
* Added `ChannelPushSettingViewModel`, `ChannelPushSettingModule`, and `ChannelPushSettingComponent`

### v3.0.0-beta.2 (Apr 29, 2022) with Chat SDK `v3.1.12`
* Supported user mention in `GroupChannel`
* Now you can send mentioning text to the other users in `GroupChannel`. These following functions are available
    * Added `setUseMention(boolean)`, `setUserMentionConfig(UserMentionConfig)`, `getUserMentionConfig()` and `isUsingMention()` in `SendbirdUIKit`
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `ChannelFragment.Builder`
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
    * Added `setMessageUIConfig(MessageUIConfig)` and `getMessageUIConfig()` in `MessageListAdapter`
    * Added `setMessageUIConfig(MessageUIConfig)` in `MessageViewHolder`
    * Added `bindUserMention(UserMentionConfig, OnMentionEventListener)` in `MessageInputComponent`
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `MessageInputComponent`
    * Added `setUseSuggestedMentionListDivider(boolean)` in `MessageInputComponent`
    * Added `notifySuggestedMentionDataChanged(List<User>)` in `MessageInputComponent`
    * Added `getMentionSuggestion()` in `ChannelViewModel`
    * Added `loadMemberList(String)` in `ChannelViewModel`

### v3.0.0-beta (Apr 12, 2022) with Chat SDK `v3.1.10`
* Support `modules` and `components` in the UIKit
* See more details and breaking changes. [[details](/changelogs/BREAKINGCHANGES_V3.md)]
* See the Migration Guide for Converting V2 to V3. [[details](/changelogs/MIGRATIONGUIDE_V3.md)]

### Up to v2.x
[Change log](/changelogs/CHANGELOG_V2.md)
