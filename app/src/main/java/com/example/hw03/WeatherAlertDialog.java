package com.example.hw03;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

public class WeatherAlertDialog extends AppCompatDialogFragment {

    AlertDialogListener listener;

    Button btn_cancel;
    Button btn_ok;
    TextView tv_alert_cityName;
    TextView tv_alert_country;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog, null);

        builder.setView(view);

        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_ok = view.findViewById(R.id.btn_ok);
        tv_alert_cityName = view.findViewById(R.id.tv_alert_cityName);
        tv_alert_country = view.findViewById(R.id.tv_alert_country);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.progressBar.setVisibility(View.VISIBLE);
                String cityName = tv_alert_cityName.getText().toString();
                String country = tv_alert_country.getText().toString();
                getDialog().cancel();
                listener.returnTexts(cityName,country);
                MainActivity.key ="";
                new MainActivity.GetKeyJSON().execute();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });

        return builder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (AlertDialogListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "Implement AlertDialogListener");
        }
    }

    public  interface AlertDialogListener{
        void returnTexts(String cityName, String country);
    }
}
