package com.angryburg.uapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.angryburg.uapp.R;
import com.angryburg.uapp.activities.HiddenSettingsActivity;
import com.angryburg.uapp.utils.NotifierService;
import com.angryburg.uapp.utils.P;

/**
 * A list of all the debug settings that you can edit
 */

public class SettingsListFragment extends Fragment implements HiddenSettingsFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View res = inflater.inflate(R.layout.hidden_settings_list, container, false);
        res.post(new Runnable() {
            @Override
            public void run() {
                ListView list = res.findViewById(R.id.settings_list);
                final String[] settings = new String[] {"Janitor Login", "Change Toolbar Color", "Always show activity back button in menu - Currently " + P.getBool("force_show_back_btn")};
                list.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, settings));
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0:
                                ((HiddenSettingsActivity) getActivity()).swapScreens(HiddenSettingsActivity.FragmentType.JANITOR_LOGIN);
                                break;
                            case 1:
                                ((HiddenSettingsActivity) getActivity()).swapScreens(HiddenSettingsActivity.FragmentType.COLOR_PICKER);
                                break;
                            case 2:
                                P.toggle("force_show_back_btn");
                                NotifierService.notify(NotifierService.NotificationType.INVALIDATE_TOOLBAR);
                                run();
                                break;
                            default:
                                GenericAlertDialogFragment.newInstance("Should never happen", getFragmentManager());
                                break;
                        }
                    }
                });
                Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
                addOptions(toolbar);
            }
        });
        return res;
    }

    @Override
    public HiddenSettingsActivity.FragmentType getType() {
        return HiddenSettingsActivity.FragmentType.SETTINGS_LIST;
    }

    /**
     * adds a close button to the menu bar
     * @param toolbar the toolbar
     */
    public void addOptions(Toolbar toolbar) {
        toolbar.setTitle(R.string.app_name);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.back_item);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getActivity().finish();
                return true;
            }
        });
    }
}
