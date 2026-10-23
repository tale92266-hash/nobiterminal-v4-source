package com.nobiterminal.app.fragments.settings.nobi;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.nobiterminal.R;
import com.nobiterminal.app.nobi.NobiTerminalUtils;
import com.nobiterminal.shared.termux.settings.properties.TermuxPropertyConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Keep
public class ExtraKeysPreferencesFragment extends PreferenceFragmentCompat {

    private static final String PRESET_CUSTOM = "custom";

    /** True while checkbox states are being set from code, to avoid save loops. */
    private boolean mUpdatingCheckBoxes = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        setPreferencesFromResource(R.xml.nobi_extra_keys_preferences, rootKey);

        ListPreference presetPreference = findPreference("nobi_extra_keys_preset");
        if (presetPreference != null) {
            presetPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                applyPreset(context, (String) newValue);
                return true;
            });
        }

        // Show the keys that are currently in effect and save every change
        // straight to ~/.termux/termux.properties.
        Set<String> currentKeys = NobiTerminalUtils.getCurrentExtraKeys();
        mUpdatingCheckBoxes = true;
        for (String key : NobiTerminalUtils.ALL_EXTRA_KEYS) {
            CheckBoxPreference checkBox = findPreference(getCheckBoxKey(key));
            if (checkBox != null)
                checkBox.setChecked(currentKeys.contains(key));
        }
        mUpdatingCheckBoxes = false;

        for (String key : NobiTerminalUtils.ALL_EXTRA_KEYS) {
            CheckBoxPreference checkBox = findPreference(getCheckBoxKey(key));
            if (checkBox != null) {
                checkBox.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (mUpdatingCheckBoxes)
                        return true;
                    Set<String> enabledKeys = new HashSet<>();
                    for (String otherKey : NobiTerminalUtils.ALL_EXTRA_KEYS) {
                        CheckBoxPreference other = findPreference(getCheckBoxKey(otherKey));
                        boolean enabled = otherKey.equals(key) ? (Boolean) newValue : (other != null && other.isChecked());
                        if (enabled)
                            enabledKeys.add(otherKey);
                    }
                    if (!saveExtraKeys(context, NobiTerminalUtils.composeExtraKeysValue(enabledKeys)))
                        return false;
                    setPresetValue(PRESET_CUSTOM);
                    return true;
                });
            }
        }
    }

    private void applyPreset(Context context, String preset) {
        switch (preset) {
            case "default":
                // Removing the property restores the built-in default bar.
                if (saveExtraKeys(context, null))
                    refreshCheckBoxes(NobiTerminalUtils.parseExtraKeysValue(TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS));
                break;
            case "developer":
                saveComposedPreset(context, new HashSet<>(Arrays.asList(
                    "ESC", "TAB", "CTRL", "ALT", "HOME", "END", "PGUP", "PGDN", "LEFT", "DOWN", "UP", "RIGHT")));
                break;
            case "minimal":
                saveComposedPreset(context, new HashSet<>(Arrays.asList(
                    "ESC", "TAB", "CTRL", "ALT", "-", "/")));
                break;
            case "off":
                if (saveExtraKeys(context, "[]"))
                    refreshCheckBoxes(new HashSet<>());
                break;
            default:
                // "custom" (or anything unknown): the toggles below define the bar.
                break;
        }
    }

    private void saveComposedPreset(Context context, Set<String> keys) {
        if (saveExtraKeys(context, NobiTerminalUtils.composeExtraKeysValue(keys)))
            refreshCheckBoxes(keys);
    }

    /** Write the value to termux.properties (null removes it) and reload styling. */
    private boolean saveExtraKeys(Context context, String value) {
        boolean saved = NobiTerminalUtils.setExtraKeysValue(value);
        if (saved)
            NobiTerminalUtils.requestStylingReload(context);
        else
            Toast.makeText(context, R.string.nobi_settings_save_failed, Toast.LENGTH_LONG).show();
        return saved;
    }

    private void refreshCheckBoxes(Set<String> enabledKeys) {
        mUpdatingCheckBoxes = true;
        for (String key : NobiTerminalUtils.ALL_EXTRA_KEYS) {
            CheckBoxPreference checkBox = findPreference(getCheckBoxKey(key));
            if (checkBox != null)
                checkBox.setChecked(enabledKeys.contains(key));
        }
        mUpdatingCheckBoxes = false;
    }

    private void setPresetValue(String value) {
        ListPreference presetPreference = findPreference("nobi_extra_keys_preset");
        if (presetPreference != null)
            presetPreference.setValue(value);
    }

    private static String getCheckBoxKey(String key) {
        return "nobi_key_" + key;
    }

}
