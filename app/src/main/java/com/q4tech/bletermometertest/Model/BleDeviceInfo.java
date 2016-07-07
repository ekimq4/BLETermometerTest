package com.q4tech.bletermometertest.Model;

/**
 * Created by ekim on 7/7/16.
 */
public class BleDeviceInfo {

    private String systemId;
    private String modelNum;
    private String serialNum;
    private String firmwareRev;
    private String hardwareRev;
    private String softwareRev;
    private String manufacturerName;
    private String regulatoryCertif;
    private String pnpId;

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


}
