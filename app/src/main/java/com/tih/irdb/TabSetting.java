package com.tih.irdb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tih.tihir.ConsumerIrManagerBase;
import com.tih.tihir.ConsumerIrManagerHtc;
import com.tih.tihir.ConsumerIrManagerIRKit;

/**
 * Created by pichu on 西元15/6/17.
 */
public class TabSetting extends Fragment{

    String tag = "TabSetting";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_setting,container,false);

        Spinner spinnerIRTransmitter = (Spinner)v.findViewById(R.id.spinner_ir_transmitter);
        Spinner spinnerIRReceiver = (Spinner)v.findViewById(R.id.spinner_ir_receiver);

//        final String[] transmitterList = {"HTC API", "IRKit"};
//        final String[] receiverList = {"HTC API", "IRKit"};

        spinnerIRReceiver.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, MainActivity.receiverCapacityList));
        spinnerIRTransmitter.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, MainActivity.transmitterCapacityList));

        spinnerIRReceiver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(tag, "Select: " + MainActivity.receiverCapacityList[position]);
                MainScreenActivity.irReceiverManager.stop();
                MainScreenActivity.irTransferManager.stop();
                if (MainActivity.receiverCapacityList[position].equals("HTC API")) {
                    MainScreenActivity.irReceiverManager = new ConsumerIrManagerHtc(getActivity());
                } else if (MainActivity.receiverCapacityList[position].startsWith("IRKit")) {
                    MainScreenActivity.irReceiverManager = new ConsumerIrManagerIRKit(getActivity());
                } else {
                    Log.e(tag, "Unknown Receiver Option: " + MainActivity.receiverCapacityList[position]);
                }
                MainScreenActivity.irReceiverManager.start();
                MainScreenActivity.irTransferManager.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(tag, "onNothingSelected");

            }
        });

        spinnerIRTransmitter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(tag, "Select: " + MainActivity.transmitterCapacityList[position]);
                MainScreenActivity.irReceiverManager.stop();
                MainScreenActivity.irTransferManager.stop();
                if (MainActivity.transmitterCapacityList[position].equals("HTC API")) {
                    MainScreenActivity.irTransferManager = new ConsumerIrManagerHtc(getActivity());
                } else if (MainActivity.transmitterCapacityList[position].startsWith("IRKit")) {
                    MainScreenActivity.irTransferManager = new ConsumerIrManagerIRKit(getActivity());
                }else if(MainActivity.transmitterCapacityList[position].equals("Android API")){
                    MainScreenActivity.irTransferManager = new ConsumerIrManagerBase(getActivity());
                }else {
                    Log.e(tag, "Unknown Receiver Option: " + MainActivity.transmitterCapacityList[position]);
                }
                MainScreenActivity.irReceiverManager.start();
                MainScreenActivity.irTransferManager.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(tag, "onNothingSelected");

            }
        });




        return v;
    }
}
