package com.backagen.peid;

import android.text.TextUtils;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Created by samwong on 14/07/2014.
 */
public final class ImmutableContactDetails implements Serializable{
    final String email;
    final String phoneNumber;

    public ImmutableContactDetails(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
