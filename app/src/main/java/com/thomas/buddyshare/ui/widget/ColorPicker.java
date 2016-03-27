package com.thomas.buddyshare.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.thomas.buddyshare.App;
import com.thomas.buddyshare.R;
import com.thomas.buddyshare.ui.event.ColorPicked;
import com.thomas.buddyshare.util.otto.ApplicationBus;


public class ColorPicker extends DialogFragment {

    private Integer[] mColorSwatchIds = {
            R.color.dark_gray,
            R.color.light_gray,
            R.color.blue,
            R.color.red,
            R.color.green,
            R.color.orange,
            R.color.yellow,
    };

    public static ColorPicker newInstance() {

        ColorPicker colorPicker = new ColorPicker();
        return colorPicker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new Dialog(getActivity(), R.style.Popup);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View dialogView = inflater.inflate(R.layout.dialog_color_picker, null);
        dialog.setContentView(dialogView);

        GridView gridView = (GridView) dialogView.findViewById(R.id.color_picker_grid);
        gridView.setAdapter(new ColorAdapter(App.getInstance(), mColorSwatchIds));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                int colorId = mColorSwatchIds[position];
                ApplicationBus.getInstance().post(new ColorPicked(colorId));
                dismiss();
            }
        });


        return dialog;
    }

    public class ColorAdapter extends BaseAdapter {
        private Context mContext;
        private Integer[] mColorIds;

        public ColorAdapter(Context c, Integer[] colorIds) {
            mContext = c;
            mColorIds = colorIds;
        }

        public int getCount() {
            return mColorIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            View colorSwatch;
            if (convertView == null) {

                colorSwatch = new View(mContext);
                colorSwatch.setLayoutParams(new GridView.LayoutParams(200, 200));
                colorSwatch.setPadding(8, 8, 8, 8);
            } else {
                colorSwatch = convertView;
            }

            colorSwatch.setBackgroundColor(getResources().getColor(mColorIds[position]));
            return colorSwatch;
        }
    }
}
