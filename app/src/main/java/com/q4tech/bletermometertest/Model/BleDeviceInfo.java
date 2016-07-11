package com.q4tech.bletermometertest.Model;

/**
 * Created by ekim on 7/7/16.
 */
public class BleDeviceInfo {

    public static final String UUID_SERVICE_DEVICE_INFO = "0000180A-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_SYSTEM_ID = "00002A23-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_MODEL_NUM = "00002A24-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_SERIAL_NUM = "00002A25-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_FIRMWARE_REV = "00002A26-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_HARDWARE_REV = "00002A27-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_SOFTWARE_REV = "00002A28-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_MANUFACTURER_NAME = "00002A29-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_REGULATORY_CERTIF = "00002A2A-0000-1000-8000-00805F9B34FB";
    public static final String UUID_CHARACTERISTIC_PNP_ID = "00002A50-0000-1000-8000-00805F9B34FB";

    private String systemId;
    private String modelNum;
    private String serialNum;
    private String firmwareRev;
    private String hardwareRev;
    private String softwareRev;
    private String manufacturerName;
    private String regulatoryCertif;
    private String pnpId;

    public BleDeviceInfo () {
        this.systemId = "";
        this.modelNum = "";
        this.serialNum = "";
        this.firmwareRev = "";
        this.hardwareRev = "";
        this.softwareRev = "";
        this.manufacturerName = "";
        this.regulatoryCertif = "";
        this.pnpId = "";
    }

    public BleDeviceInfo(String systemId, String modelNum, String serialNum,
                         String firmwareRev, String hardwareRev, String softwareRev,
                         String manufacturerName, String regulatoryCertif, String pnpId) {
        this.systemId = systemId;
        this.modelNum = modelNum;
        this.serialNum = serialNum;
        this.firmwareRev = firmwareRev;
        this.hardwareRev = hardwareRev;
        this.softwareRev = softwareRev;
        this.manufacturerName = manufacturerName;
        this.regulatoryCertif = regulatoryCertif;
        this.pnpId = pnpId;
    }

    public void refreshValues() {
        this.systemId = "";
        this.modelNum = "";
        this.serialNum = "";
        this.firmwareRev = "";
        this.hardwareRev = "";
        this.softwareRev = "";
        this.manufacturerName = "";
        this.regulatoryCertif = "";
        this.pnpId = "";
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getModelNum() {
        return this.modelNum;
    }

    public void setModelNum(String modelNum) {
        this.modelNum = modelNum;
    }

    public String getSerialNum() {
        return this.serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getFirmwareRev() {
        return this.firmwareRev;
    }

    public void setFirmwareRev(String firmwareRev) {
        this.firmwareRev = firmwareRev;
    }

    public String getHardwareRev() {
        return this.hardwareRev;
    }

    public void setHardwareRev(String hardwareRev) {
        this.hardwareRev = hardwareRev;
    }

    public String getSoftwareRev() {
        return this.softwareRev;
    }

    public void setSoftwareRev(String softwareRev) {
        this.softwareRev = softwareRev;
    }

    public String getManufacturerName() {
        return this.manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getRegulatoryCertif() {
        return this.regulatoryCertif;
    }

    public void setRegulatoryCertif(String regulatoryCertif) {
        this.regulatoryCertif = regulatoryCertif;
    }

    public String getPnpId() {
        return this.pnpId;
    }

    public void setPnpId(String pnpId) {
        this.pnpId = pnpId;
    }


}
