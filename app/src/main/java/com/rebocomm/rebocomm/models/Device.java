package com.rebocomm.rebocomm.models;

public class Device {
    public String deviceName;
	public String deviceAddress;

    @Override
    public String toString() {
        return "Device{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                '}';
    }
}