package com.tangan.glucose.dto;

public final class SettingsDtos {
    private SettingsDtos() { }
    public record Response(boolean glucoseReminder, boolean medicationReminder, boolean familyAlert, boolean autoSync, boolean faceIdUnlock) { }
    public record Request(Boolean glucoseReminder, Boolean medicationReminder, Boolean familyAlert, Boolean autoSync, Boolean faceIdUnlock) { }
}
