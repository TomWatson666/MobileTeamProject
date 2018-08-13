package team5project.treasurehuntapp;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

/**
 * Created by joebr on 21/04/2017.
 */

public class QRFragment extends ListFragment {

    private String treasureHunt;
    private List<String> qrCodes = new ArrayList<>();
    static String selectedQRCode;
    static List<String> names = new ArrayList<>();
    static int index;
    public ArrayAdapter<String> list;

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (int i = 0; i < DataVault.viewedTreasureHuntLocations.size(); i++) {
            qrCodes.add(DataVault.locations.get(i).getQrCode());
        }

        names.clear();

        for(int i = 0; i < DataVault.viewedTreasureHuntLocations.size(); i++) {
            names.add(DataVault.locations.get(i).getName());
        }

        list = new ArrayAdapter<String>(getActivity(),//assigns all of the values to the list
                android.R.layout.simple_list_item_activated_1,
                names);

        setListAdapter(list);

        getListView().setChoiceMode(CHOICE_MODE_SINGLE);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        index = getListView().getCheckedItemPosition();//on click, returns the index of the clicked item
        selectedQRCode = getListView().getItemAtPosition(index).toString();//returns the clicked item
    }

}
