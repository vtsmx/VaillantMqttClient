package org.vmqtt;

import java.time.LocalDateTime;

public class Temperature
{
    public float value;
    public String id;
    public String datetime;
    public String unit;

    public Temperature(String id, float value, String datetime, String unit)
    {
        this.value = value;
        this.id = id;
        this.datetime = datetime;
        this.unit = unit;
    }
}
