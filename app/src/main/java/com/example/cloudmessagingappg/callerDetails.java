package com.example.cloudmessagingappg;

import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;

public class callerDetails
{
    public static CallClient xcallClient;
    public static Call xcall;


    public static CallClient getXcallClient() {
        return xcallClient;
    }

    public static void setXcallClient(CallClient xcallClient) {
        callerDetails.xcallClient = xcallClient;
    }

    public static Call getXcall() {
        return xcall;
    }

    public static void setXcall(Call xcall) {
        callerDetails.xcall = xcall;
    }
}
