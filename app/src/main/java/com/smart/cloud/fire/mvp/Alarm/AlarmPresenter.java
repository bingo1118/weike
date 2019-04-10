package com.smart.cloud.fire.mvp.Alarm;

import android.content.Context;
import android.provider.Settings;

import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.AlarmMessageModel;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpError;
import com.smart.cloud.fire.rxjava.ApiCallback;
import com.smart.cloud.fire.rxjava.RxTimeCount;
import com.smart.cloud.fire.rxjava.SubscriberCallBack;
import com.smart.cloud.fire.utils.MusicManger;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/9/27.
 */
public class AlarmPresenter extends BasePresenter<AlarmView>{
    private boolean isAlarm;
    public AlarmPresenter(AlarmView view){
        attachView(view);
    }

    public void finishActivity(int count, final Context context){
        RxTimeCount.countdown(count)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        startMusic();
                        loadMusicAndVibrate(context);
                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        mvpView.finishActivity();
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopMusic();
                    }

                    @Override
                    public void onNext(Integer integer) {
                    }
                });
    }
    private void loadMusicAndVibrate(Context context) {
        long lastclosetime=SharedPreferencesManager.getInstance().getLongData(context,
                "AlarmVoice",
                "closealarmvoice");//@@
        if((System.currentTimeMillis()-lastclosetime)<60*1000){
            return;
        }//@@
        MusicManger.getInstance().playAlarmMusic(context);
        new Thread() {
            public void run() {
                while (isAlarm) {
                    MusicManger.getInstance().Vibrate();
                    Utils.sleepThread(100);
                }
                MusicManger.getInstance().stopVibrate();
            }
        }.start();
    }

    public void disposeAlarm(String userId,String alarmSerialNumber){
        Observable mObservable = apiStores1.textAlarmAck(userId,alarmSerialNumber);
        addSubscription(mObservable,new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                mvpView.finishRequest();
                T.showShort(MyApp.app,"已处理");
            }

            @Override
            public void onFailure(int code, String msg) {
            }

            @Override
            public void onCompleted() {
            }
        }));
    }

    public void startMusic(){
        this.isAlarm=true;
    }

    public void stopMusic(){
        this.isAlarm=false;
    }

    public void telPhone(Context mContext,String phoneNum){
       telPhoneAction(mContext,phoneNum);
    }

    public void dealAlarm(String userId, String smokeMac){//@@5.19添加取消报警信息的位置
        Observable mObservable = apiStores1.dealAlarm(userId,smokeMac);
        addSubscription(mObservable,new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                int errorCode = model.getErrorCode();
                if(errorCode==0){
                    mvpView.getData("取消成功");
                }else{
                    mvpView.getData("取消失败");
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                mvpView.getData("网络错误");
            }

            @Override
            public void onCompleted() {
            }
        }));
    }



}
