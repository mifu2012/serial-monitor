package org.mif.serial.monitor.vo;

import lombok.Data;

/**
 * @description: 设备信息
 * @author: mif
 * @date: 2019/5/3 22:16
 */
@Data
public class EquipmentVO  {


    private String equipmentNo;

    private String samplingPeriod;

    private String baudRate;

    private String equipmentLength;

    private String parityBit;

    private String stopBit;

    private String linkedMethod;

    private String channelId;


}
