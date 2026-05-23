package com.smart.parking.auth;

import lombok.Data;

@Data
public class UpdatePreferencesRequest {
    private Boolean notificationsEnabled;
    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
    private String preferredLanguage;
    private Integer reminderMinutesBeforeEnd;
}