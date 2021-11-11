package com.hsu.davincicode;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserInfoViewModel extends ViewModel {
    private MutableLiveData<NetworkObj> networkObj = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();

    public void init(NetworkObj networkObj, String userName) {
        this.networkObj.postValue(networkObj);
        this.userName.postValue(userName);
    }

    public MutableLiveData<NetworkObj> getNetworkObj() {
        return networkObj;
    }

    public void setNetworkObj(NetworkObj networkObj) {
        this.networkObj.setValue(networkObj);
    }

    public MutableLiveData<String> getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.setValue(userName);
    }
}
