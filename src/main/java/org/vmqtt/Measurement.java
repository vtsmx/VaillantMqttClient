package org.vmqtt;

public class Measurement
{
    public float value;
    public String id;
    public String datetime;
    public String unit;

    public Measurement(String id, float value, String datetime, String unit)
    {
        this.value = value;
        this.id = id;
        this.datetime = datetime;
        this.unit = unit;
    }
}
