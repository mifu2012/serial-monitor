package org.mif.serial.monitor.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: MiF
 * @date: 2019-08-06 15:27
 * @copyRight: © 2019 LvDoo
 */
public class EqMaps {

    public static final Map<String, String> EQUIPMENT_LENGTH_MAP = new HashMap<>();
    public static final Map<String, String> PARITY_BIT_MAP = new HashMap<>();
    public static final Map<String, String> STOP_BIT_MAP = new HashMap<>();
    public static final Map<String, String> LINKED_METHOD_MAP = new HashMap<>();

    static {

        EQUIPMENT_LENGTH_MAP.put("0x0000", "8位");
        EQUIPMENT_LENGTH_MAP.put("0x1000", "9位");
        EQUIPMENT_LENGTH_MAP.put("", "");

        PARITY_BIT_MAP.put("0x0000", "无校验位");
        PARITY_BIT_MAP.put("0x0400", "偶校验位");
        PARITY_BIT_MAP.put("0x0500", "奇校验位");
        PARITY_BIT_MAP.put("", "");

        STOP_BIT_MAP.put("0x0000", "0.5位");
        STOP_BIT_MAP.put("0x1000", "1位");
        STOP_BIT_MAP.put("0x3000", "1.5位");
        STOP_BIT_MAP.put("0x2000", "2位");
        STOP_BIT_MAP.put("", "");

        LINKED_METHOD_MAP.put("0x00", "485");
        LINKED_METHOD_MAP.put("0x01", "422");
        LINKED_METHOD_MAP.put("", "");
    }
}
