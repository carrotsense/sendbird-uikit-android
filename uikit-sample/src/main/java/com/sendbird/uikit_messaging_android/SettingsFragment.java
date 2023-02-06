package com.sendbird.uikit_messaging_android;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.params.UserUpdateParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.modules.components.StateHeaderComponent;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.PermissionUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit_messaging_android.consts.StringSet;
import com.sendbird.uikit_messaging_android.databinding.FragmentSettingsBinding;
import com.sendbird.uikit_messaging_android.databinding.ViewCustomMenuTextButtonBinding;
import com.sendbird.uikit_messaging_android.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Displays a settings screen.
 */
public class SettingsFragment extends Fragment {
    private final String[] REQUIRED_PERMISSIONS = PermissionUtils.CAMERA_PERMISSION;

    private FragmentSettingsBinding binding;
    private final StateHeaderComponent headerComponent = new StateHeaderComponent();
    private Uri mediaUri;

    private final ActivityResultLauncher<Intent> appSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), intent -> {
        if (getContext() == null) return;
        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
        if (hasPermission) {
            showMediaSelectDialog();
        }
    });
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
        if (PermissionUtils.getNotGrantedPermissions(permissionResults).isEmpty()) {
            showMediaSelectDialog();
        }
    });

    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || getContext() == null) return;
        final Uri mediaUri = this.mediaUri;
        if (mediaUri != null) {
            final File file = FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);
            updateUserProfileImage(file);
        }
    });
    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null || getContext() == null) return;
        final Uri mediaUri = intent.getData();
        if (mediaUri != null) {
            final File file = FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);
            updateUserProfileImage(file);
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SendbirdUIKit.connect(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        headerComponent.getParams().setTitle(getString(R.string.text_tab_settings));
        headerComponent.getParams().setUseLeftButton(false);
        headerComponent.getParams().setRightButtonText(getString(R.string.text_settings_header_edit_button));
        final View header = headerComponent.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPage();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem editMenuItem = menu.findItem(R.id.action_edit_profile);
        ViewCustomMenuTextButtonBinding binding = ViewCustomMenuTextButtonBinding.inflate(getLayoutInflater());
        boolean isDark = PreferenceUtils.isUsingDarkTheme();
        binding.text.setText(R.string.text_settings_header_edit_button);
        binding.text.setBackgroundResource(isDark ? R.drawable.sb_button_uncontained_background_dark : R.drawable.sb_button_uncontained_background_light);
        binding.text.setTextColor(getResources().getColor(isDark ? R.color.primary_200 : R.color.primary_300));
        View rootView = binding.getRoot();
        rootView.setOnClickListener(v -> onOptionsItemSelected(editMenuItem));
        editMenuItem.setActionView(rootView);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit_profile) {
            Logger.d("++ edit button clicked");
            showEditProfileDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);
    }

    private void requestPermission(@NonNull String[] permissions) {
        if (getContext() == null || getActivity() == null) return;
        // 1. check permission
        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), permissions);
        if (hasPermission) {
            showMediaSelectDialog();
            return;
        }

        // 2. determine whether rationale popup should show
        final List<String> deniedList = PermissionUtils.getExplicitDeniedPermissionList(getActivity(), permissions);
        if (!deniedList.isEmpty()) {
            showPermissionRationalePopup(deniedList.get(0));
            return;
        }
        // 3. request permission
        this.permissionLauncher.launch(permissions);
    }

    private void showPermissionRationalePopup(@NonNull String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
        builder.setMessage(getPermissionGuideMessage(requireContext(), permission));
        builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            appSettingLauncher.launch(intent);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), com.sendbird.uikit.R.color.secondary_300));
    }

    private void initPage() {
        if (getContext() == null) {
            return;
        }

        boolean isDark = PreferenceUtils.isUsingDarkTheme();
        binding.background.setBackgroundResource(isDark ? R.color.background_600 : R.color.background_50);
        binding.tvNickname.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.idDivider.setBackgroundResource(isDark ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light);
        binding.tvUserIdBadge.setTextColor(getResources().getColor(isDark ? R.color.ondark_02 : R.color.onlight_02));
        binding.tvUserId.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.profileDivider.setBackgroundResource(isDark ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light);
        binding.itemDarkTheme.setBackgroundResource(isDark ? R.drawable.selector_rectangle_dark600 : R.drawable.selector_rectangle_light);
        binding.tvDarkThemeName.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.darkThemeDivider.setBackgroundResource(isDark ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light);
        binding.itemDisturb.setBackgroundResource(isDark ? R.drawable.selector_rectangle_dark600 : R.drawable.selector_rectangle_light);
        binding.tvDisturbName.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.disturbDivider.setBackgroundResource(isDark ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light);
        binding.itemHome.setBackgroundResource(isDark ? R.drawable.selector_rectangle_dark600 : R.drawable.selector_rectangle_light);
        binding.tvHomeName.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.signOutDivider.setBackgroundResource(isDark ? R.drawable.sb_line_divider_dark : R.drawable.sb_line_divider_light);

        boolean useHeader = true;
        boolean useDoNotDisturb = true;
        if (getArguments() != null) {
            useHeader = getArguments().getBoolean(StringSet.SETTINGS_USE_HEADER, true);
            useDoNotDisturb = getArguments().getBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, true);
        }

        if (headerComponent.getRootView() != null) {
            headerComponent.getRootView().setVisibility(useHeader ? View.VISIBLE : View.GONE);
        }
        headerComponent.setOnRightButtonClickListener(v -> {
            Logger.d("++ edit button clicked");
            showEditProfileDialog();
        });

        if (SendbirdChat.getCurrentUser() != null) {
            loadUserProfileUrl(SendbirdChat.getCurrentUser().getProfileUrl());
            binding.tvUserId.setText(SendbirdChat.getCurrentUser().getUserId());
            binding.tvNickname.setText(SendbirdChat.getCurrentUser().getNickname());
        } else {
            loadUserProfileUrl(PreferenceUtils.getProfileUrl());
            binding.tvUserId.setText(PreferenceUtils.getUserId());
            binding.tvNickname.setText(PreferenceUtils.getNickname());
        }

        final boolean isDarkMode = PreferenceUtils.isUsingDarkTheme();
        int switchTrackTint = isDarkMode ? com.sendbird.uikit.R.color.sb_switch_track_dark : com.sendbird.uikit.R.color.sb_switch_track_light;
        int switchThumbTint = isDarkMode ? com.sendbird.uikit.R.color.sb_switch_thumb_dark : com.sendbird.uikit.R.color.sb_switch_thumb_light;

        binding.itemDarkTheme.setOnClickListener(v -> {
            Logger.d("++ dark theme clicked");
            updateDarkTheme();
        });


        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.background_700 : R.color.background_50;
        int themeBackgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_300 : R.color.background_400;
        binding.ivDarkThemeIcon.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_theme, iconTint));
        binding.ivDarkThemeIcon.setBackgroundDrawable(DrawableUtils.setTintList(getContext(), R.drawable.shape_oval, themeBackgroundTint));
        binding.scDarkThemeSwitch.setTrackTintList(AppCompatResources.getColorStateList(getContext(), switchTrackTint));
        binding.scDarkThemeSwitch.setThumbTintList(AppCompatResources.getColorStateList(getContext(), switchThumbTint));
        binding.scDarkThemeSwitch.setChecked(PreferenceUtils.isUsingDarkTheme());
        SendbirdUIKit.setDefaultThemeMode(PreferenceUtils.isUsingDarkTheme() ?
                SendbirdUIKit.ThemeMode.Dark : SendbirdUIKit.ThemeMode.Light);
        binding.scDarkThemeSwitch.setOnClickListener(v -> {
            Logger.d("++ dark theme clicked");
            updateDarkTheme();
        });

        int disturbBackgroundTint = SendbirdUIKit.isDarkMode() ? R.color.secondary_200 : R.color.secondary_300;
        binding.ivDisturbIcon.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_notifications_filled, iconTint));
        binding.ivDisturbIcon.setBackgroundDrawable(DrawableUtils.setTintList(getContext(), R.drawable.shape_oval, disturbBackgroundTint));
        binding.itemDisturb.setVisibility(useDoNotDisturb ? View.VISIBLE : View.GONE);
        if (useDoNotDisturb) {
            binding.itemDisturb.setOnClickListener(v -> {
                Logger.d("++ disturb clicked");
                updateDoNotDisturb();
            });

            binding.scDisturbSwitch.setTrackTintList(AppCompatResources.getColorStateList(getContext(), switchTrackTint));
            binding.scDisturbSwitch.setThumbTintList(AppCompatResources.getColorStateList(getContext(), switchThumbTint));
            SendbirdChat.getDoNotDisturb((b, i, i1, i2, i3, s, e) -> {
                PreferenceUtils.setDoNotDisturb(b);
                if (isActive()) {
                    binding.scDisturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
                }
            });
            binding.scDisturbSwitch.setOnClickListener(v -> {
                Logger.d("++ disturb clicked");
                updateDoNotDisturb();
            });
        }

        int homeBackgroundTint = SendbirdUIKit.getDefaultThemeMode().getErrorColorResId();
        binding.ivHomeIcon.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_leave, iconTint));
        binding.ivHomeIcon.setBackgroundDrawable(DrawableUtils.setTintList(getContext(), R.drawable.shape_oval, homeBackgroundTint));
        binding.itemHome.setOnClickListener(v -> {
            Logger.d("++ home clicked");
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void showEditProfileDialog() {
        if (getContext() == null) return;

        DialogListItem[] items = {
                new DialogListItem(R.string.text_settings_change_user_nickname),
                new DialogListItem(R.string.text_settings_change_user_profile_image)
        };

        DialogUtils.showListBottomDialog(requireContext(), items, (v, p, item) -> {
            final int key = item.getKey();
            if (key == R.string.text_settings_change_user_nickname) {
                Logger.dev("change user nickname");
                OnEditTextResultListener listener = result -> {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    updateUserNickname(result);
                };

                DialogEditTextParams params = new DialogEditTextParams(getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_name_hint));
                params.setEnableSingleLine(true);
                DialogUtils.showInputDialog(
                        requireContext(),
                        getString(R.string.text_settings_change_user_nickname),
                        params, listener,
                        getString(com.sendbird.uikit.R.string.sb_text_button_save), null,
                        getString(com.sendbird.uikit.R.string.sb_text_button_cancel), null);
            } else if (key == R.string.text_settings_change_user_profile_image) {
                Logger.dev("change user profile");

                if (getContext() == null) return;
                final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
                if (hasPermission) {
                    showMediaSelectDialog();
                    return;
                }

                requestPermission(REQUIRED_PERMISSIONS);
            }
        });
    }

    private void updateUserNickname(@NonNull String nickname) {
        if (getContext() == null) return;
        WaitingDialog.show(getContext());
        final UserUpdateParams params = new UserUpdateParams();
        params.setNickname(nickname);
        SendbirdUIKit.updateUserInfo(params, e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                ContextUtils.toastError(getContext(), R.string.text_error_update_user_information);
                return;
            }

            PreferenceUtils.setNickname(nickname);
            if (isActive()) {
                binding.tvNickname.setText(nickname);
            }
        });
    }

    private void updateUserProfileImage(@NonNull File profileImage) {
        if (getContext() == null) return;
        WaitingDialog.show(getContext());
        final UserUpdateParams params = new UserUpdateParams();
        params.setProfileImageFile(profileImage);
        SendbirdChat.updateCurrentUserInfo(params, e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                ContextUtils.toastError(getContext(), R.string.text_error_update_user_information);
                return;
            }

            final User currentUser = SendbirdChat.getCurrentUser();
            if (currentUser != null) {
                final String profileUrl = currentUser.getProfileUrl();
                PreferenceUtils.setProfileUrl(profileUrl);
                loadUserProfileUrl(profileUrl);
            }
        });
    }

    private void updateDarkTheme() {
        SendbirdUIKit.setDefaultThemeMode(SendbirdUIKit.isDarkMode() ?
                SendbirdUIKit.ThemeMode.Light : SendbirdUIKit.ThemeMode.Dark);
        PreferenceUtils.setUseDarkTheme(SendbirdUIKit.isDarkMode());
        binding.scDarkThemeSwitch.setChecked(SendbirdUIKit.isDarkMode());
        if (getActivity() != null) {
            getActivity().finish();
            startActivity(getActivity().getIntent());
        }
    }

    private void updateDoNotDisturb() {
        binding.scDisturbSwitch.setChecked(!PreferenceUtils.getDoNotDisturb());
        Logger.d("update do not disturb : " + !PreferenceUtils.getDoNotDisturb());
        SendbirdChat.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb(), 0, 0, 23, 59, TimeZone.getDefault().getID(), e -> {
            if (e != null) {
                ContextUtils.toastError(getContext(), R.string.text_error_update_do_not_disturb);
                if (isActive()) {
                    binding.scDisturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
                }
                return;
            }
            Logger.d("update do not disturb on callback : " + !PreferenceUtils.getDoNotDisturb());
            PreferenceUtils.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb());
        });
    }

    private void showMediaSelectDialog() {
        if (getContext() == null) return;
        DialogListItem[] items = {
                new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera),
                new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery)};

        DialogUtils.showListDialog(requireContext(),
                getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image),
                items, (v, p, item) -> {
                    try {
                        final int key = item.getKey();
                        if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                            takeCamera();
                        } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                            takePhoto();
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                });
    }

    private void takeCamera() {
        SendbirdChat.setAutoBackgroundDetection(false);
        this.mediaUri = FileUtils.createImageFileUri(requireContext());
        if (mediaUri == null) return;
        Intent intent = IntentUtils.getCameraIntent(requireContext(), mediaUri);
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent);
        }
    }

    private void takePhoto() {
        SendbirdChat.setAutoBackgroundDetection(false);
        Intent intent = IntentUtils.getImageGalleryIntent();
        getContentLauncher.launch(intent);
    }

    private static String getPermissionGuideMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }

    protected boolean isActive() {
        boolean isDeactivated = isRemoving() || isDetached() || getContext() == null;
        return !isDeactivated;
    }

    private void loadUserProfileUrl(final String url) {
        if (isActive()) {
            Glide.with(requireContext())
                    .load(url)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(DrawableUtils.setTintList(requireContext(), R.drawable.icon_user, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()))
                    .into(binding.ivProfileView);
        }
    }
}
