/*
 * HTC Corporation Proprietary Rights Acknowledgment
 *
 * Copyright (C) 2013 HTC Corporation
 *
 * All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.tih.tihir;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.getirkit.irkit.IRKit;
import com.getirkit.irkit.IRPeripheral;
import com.getirkit.irkit.IRPeripherals;
import com.getirkit.irkit.IRSignal;
import com.getirkit.irkit.IRSignals;
import com.getirkit.irkit.net.IRAPICallback;
import com.getirkit.irkit.net.IRAPIError;
import com.getirkit.irkit.net.IRAPIResult;
import com.getirkit.irkit.net.IRHTTPClient;
import com.getirkit.irkit.net.IRInternetAPIService;
import com.htc.circontrol.CIRControl;
import com.htc.htcircontrol.HtcIrData;

import java.lang.ref.WeakReference;
import java.util.UUID;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConsumerIrManagerIRKit extends ConsumerIrManagerCompat {

	private static final String TAG = "ConsumerIrManagerIRKit";
//	private CIRControl mControl;
	private Context mContext;
	private HtcIrData mLearntKey;

//    IrHandler mHandler = new IrHandler(Looper.getMainLooper());
	public ConsumerIrManagerIRKit(Context context) {
		super(context);
		mContext = context;

        IRKit.sharedInstance().init(context.getApplicationContext());

		//supportedAPIs = supportedAPIs & HTCSUPPORT;
	}

    @Override
    public void setOnLearnListener(OnLearnListener listener){
        mOnLearnListener = listener;
    }

	@Override
	public void transmit(int carrierFrequency, int[] pattern) {
        Log.d(TAG, "Transmit");
        IRPeripherals peripherals = IRKit.sharedInstance().peripherals;
        if(peripherals.size() == 0){
            return;
        }
        IRSignal signal = new IRSignal();
        for(int i=0;i<pattern.length;++i){
            pattern[i] *=2;
        }
        signal.setDeviceId(peripherals.get(0).getDeviceId());
        signal.setData(pattern);
        signal.setFormat("raw");
        signal.setFrequency(carrierFrequency/1000);
        // signalを送信
        IRKit.sharedInstance().sendSignal(signal, null);

	}

	@Override
	public CarrierFrequencyRange[] getCarrierFrequencies() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			//TODO: call getCarrierFrequencies() via reflection in this case
			// or call standard API from a separate activity
		} else {
			Log.i(TAG,"getCarrierFrequencies() is not available via the HTC CIR APIs");
		}
		return null;
	}

	@Override
	public boolean hasIrEmitter() {
        IRPeripherals peripherals = IRKit.sharedInstance().peripherals;
        return peripherals.size() != 0;
    }
	
	@Override
	public UUID learnIRCmd(int timeout) {
        IRPeripheral peripheral = IRKit.sharedInstance().peripherals.get(0);

        if (peripheral.isLocalAddressResolved()) {
            // このperipheralに対してDevice HTTP APIを利用できる
            Log.d(TAG, "このperipheralに対してDevice HTTP APIを利用できる");


            // IRHTTPClientインスタンスを取得
            IRHTTPClient httpClient = IRKit.sharedInstance().getHTTPClient();

// 赤外線信号を受信する。第2引数にtrueを指定すると、
// 前回の赤外線信号を消去して新しい赤外線信号を待機する。
            httpClient.waitForSignal(new IRAPICallback<IRInternetAPIService.GetMessagesResponse>() {
                @Override
                public void success(IRInternetAPIService.GetMessagesResponse getMessagesResponse, Response response) {
                    // 信号の受信に成功した

                    if(mOnLearnListener == null)return;

                    // 受信した信号を保存する例
                    IRSignals signals = IRKit.sharedInstance().signals;
                    IRSignal signal = new IRSignal();
                    signal.setId(signals.getNewId());
                    signal.setDeviceId(getMessagesResponse.deviceid);
                    signal.setFrequency((float) getMessagesResponse.message.freq);
                    signal.setFormat(getMessagesResponse.message.format);
                    signal.setData(getMessagesResponse.message.data);
                    for(int i = 0;i < getMessagesResponse.message.data.length;++i){
                        getMessagesResponse.message.data[i] /= 2;
                    }

                    mOnLearnListener.onLearn(signal.toJson());

                }

                @Override
                public void failure(RetrofitError error) {
                    if(mOnLearnListener != null){
                        mOnLearnListener.onError(error.getMessage());
                    }
                    // エラー
                }
            }, true);


        } else {
            // このperipheralに対してDevice HTTP APIを利用できない
            Log.d(TAG, "このperipheralに対してDevice HTTP APIを利用できない");
            Log.w(TAG, "Unimplemented");
        }
		return null;
	}
	@Override
	public void start() {


        IRKit irkit = IRKit.sharedInstance();

        // ローカルネットワーク内のIRKit検索を開始
        // Start for discovering IRKit in local network
        // 開始搜尋區域網路內的IRKit
        irkit.startServiceDiscovery();

        // Wi-Fi接続状態の変化を監視して、Wi-Fiが有効になった際に
        // IRKit検索を開始し、Wi-Fiが無効になった際に検索を停止する
        // Monitoring Wi-Fi state, Start for discovery IRKit
        // when Wi-Fi Connected, Stop when Wi-Fi Disconnect.
        // 監控Wi-Fi的連接狀況，Wi-Fi連接時開始搜索IRKit，Wi-Fi
        // 斷掉時停止搜索。
        irkit.registerWifiStateChangeListener();

        // clientkeyを取得していない場合は取得する
        // Get clientkey if do not have one.
        // 假如還沒拿到clientkey就拿一個
        irkit.registerClient();

	}	
	@Override
	public void stop() {
        IRKit irkit = IRKit.sharedInstance();

        // ローカルネットワーク内のIRKit検索を停止
        // Stop for discovering IRKit in local network
        // 停止搜尋區域網路內的IRKit
        irkit.stopServiceDiscovery();

        // Wi-Fi状態の変化の監視をやめる
        // Stop for monitoring Wi-Fi State
        // 停止監視Wi-Fi狀態
        irkit.unregisterWifiStateChangeListener();
	}		
	@Override
	public boolean isStarted() {
        return false;
	}
	@Override
	public UUID cancelCommand() {
        Log.w(TAG, "Unimplemented cancelCommand");
		return null;
	}
	@Override
	public UUID discardCommand(UUID uuid) {
        Log.w(TAG, "Unimplemented discardCommand");
		return null;
	}

}
