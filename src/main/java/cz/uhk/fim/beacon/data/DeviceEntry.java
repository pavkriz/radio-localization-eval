/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.uhk.fim.beacon.data;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class DeviceEntry {

    // Variables of this class
    private String type;                // Device type (phone, wear, TV ...)
    @SerializedName("id")
    private String deviceId;            // Either a changelist number, or a label like "M4-rc20".
    @SerializedName("device")
    private String deviceName;          // The name of the industrial design.
    private String model;               // The end-user-visible name for the end product.
    private String brand;               // The consumer-visible brand with which the product/hardware will be associated, if any.
    private String manufacturer;        // The manufacturer of the product/hardware.
    private String display;             // A build ID string meant for displaying to the user
    private String hardware;            // The name of the hardware (from the kernel command line or /proc).
    @SerializedName("serial")
    private String serialNumber;        // Gets the hardware serial, if available.
    @SerializedName("fingerprint")
    private String deviceFingerprint;   // A string that uniquely identifies this build.
    private String os;                  // CODENAME-RELEASE = The current development codename and the user-visible version string.
    private int api;                    // The user-visible SDK version of the framework; its possible values are defined in Build.VERSION_CODES.

    // Default constructor used for Gson
    public DeviceEntry() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceEntry deviceEntry = (DeviceEntry) o;
        return Objects.equals(this.type, deviceEntry.type) &&
                Objects.equals(this.deviceId, deviceEntry.deviceId) &&
                Objects.equals(this.deviceName, deviceEntry.deviceName) &&
                Objects.equals(this.model, deviceEntry.model) &&
                Objects.equals(this.brand, deviceEntry.brand) &&
                Objects.equals(this.manufacturer, deviceEntry.manufacturer) &&
                Objects.equals(this.display, deviceEntry.display) &&
                Objects.equals(this.hardware, deviceEntry.hardware) &&
                Objects.equals(this.serialNumber, deviceEntry.serialNumber) &&
                Objects.equals(this.deviceFingerprint, deviceEntry.deviceFingerprint) &&
                Objects.equals(this.os, deviceEntry.os) &&
                Objects.equals(this.api, deviceEntry.api);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, deviceId, deviceName, model, brand, manufacturer, display, hardware, serialNumber, deviceFingerprint, os, api );
    }


    @Override
    public String toString() {
        return "class CellularEntry {\n" +
                "    type: " + toIndentedString(type) + "\n" +
                "    deviceId: " + toIndentedString(deviceId) + "\n" +
                "    deviceName: " + toIndentedString(deviceName) + "\n" +
                "    model: " + toIndentedString(model) + "\n" +
                "    brand: " + toIndentedString(brand) + "\n" +
                "    manufacturer: " + toIndentedString(manufacturer) + "\n" +
                "    display: " + toIndentedString(display) + "\n" +
                "    hardware: " + toIndentedString(hardware) + "\n" +
                "    serialNumber: " + toIndentedString(serialNumber) + "\n" +
                "    deviceFingerprint: " + toIndentedString(deviceFingerprint) + "\n" +
                "    os: " + toIndentedString(os) + "\n" +
                "    api: " + toIndentedString(api) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}