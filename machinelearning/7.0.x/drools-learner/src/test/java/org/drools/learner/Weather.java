package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.tools.FieldAnnotation;

public class Weather {
    private String outlook;
    private String temperature;
    private String humidity;
    private String wind;

    public static Domain playDomain = new Domain(Weather.class, "play", String.class);
    public static Domain outlookDomain = new Domain(Weather.class, "outlook", String.class);
    public static Domain temperatureDomain = new Domain(Weather.class, "temperature", String.class);
    public static Domain humidityDomain = new Domain(Weather.class, "humidity", String.class);

    static {
        playDomain.addCategory("no");
        playDomain.addCategory("yes");

        outlookDomain.addCategory("sun");
        outlookDomain.addCategory("overcast");
        outlookDomain.addCategory("rain");

        temperatureDomain.addCategory("hot");
        temperatureDomain.addCategory("cold");
        temperatureDomain.addCategory("mild");

        humidityDomain.addCategory("high");
        humidityDomain.addCategory("normal");
        humidityDomain.addCategory("low");
    }



    @FieldAnnotation(target = true)
    private String play;

    public Weather(String outlook, String temperature, String humidity, String wind, String play) {
        this.outlook = outlook;
        this.temperature = temperature;
        this.humidity = humidity;
        this.wind = wind;
        this.play = play;
    }

    public String getOutlook() {
        return outlook;
    }

    public void setOutlook(String outlook) {
        this.outlook = outlook;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getPlay() {
        return play;
    }

    public void setPlay(String play) {
        this.play = play;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Weather weather = (Weather) o;

        if (!outlook.equals(weather.outlook)) {
            return false;
        }
        if (!temperature.equals(weather.temperature)) {
            return false;
        }
        if (!humidity.equals(weather.humidity)) {
            return false;
        }
        if (!wind.equals(weather.wind)) {
            return false;
        }
        return play.equals(weather.play);
    }

    @Override public int hashCode() {
        int result = outlook.hashCode();
        result = 31 * result + temperature.hashCode();
        result = 31 * result + humidity.hashCode();
        result = 31 * result + wind.hashCode();
        result = 31 * result + play.hashCode();
        return result;
    }

    public static InstanceList getData() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Object> list = getDataAsList();

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static List<Object> getDataAsList() {
        List<Object> list = new ArrayList<Object>();
        list.add(new Weather("sun", "hot", "high", "low", "no"));
        list.add(new Weather("sun", "hot", "high", "high", "no"));
        list.add(new Weather("overcast", "hot", "high", "low", "yes"));
        list.add(new Weather("rain", "mild", "high", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "high", "no"));
        list.add(new Weather("overcast", "cold", "normal", "high", "yes"));
        list.add(new Weather("sun", "mild", "high", "low", "no"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "normal", "low", "yes"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));
        list.add(new Weather("overcast", "mild", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "high", "high", "no"));
        return list;
    }

    public static InstanceList getDataMoreYes() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "yes"));
        list.add(new Weather("sun", "hot", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "high", "low", "yes"));
        list.add(new Weather("rain", "mild", "high", "low", "no"));
        list.add(new Weather("rain", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "high", "no"));
        list.add(new Weather("overcast", "cold", "normal", "high", "yes"));
        list.add(new Weather("sun", "mild", "high", "low", "no"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "normal", "low", "no"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));
        list.add(new Weather("overcast", "mild", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "high", "high", "no"));

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static InstanceList getDataMoreNo() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "no"));
        list.add(new Weather("sun", "hot", "high", "high", "no"));
        list.add(new Weather("overcast", "hot", "high", "low", "no"));
        list.add(new Weather("rain", "mild", "high", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "high", "no"));
        list.add(new Weather("overcast", "cold", "normal", "high", "yes"));
        list.add(new Weather("sun", "mild", "high", "low", "no"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "normal", "low", "no"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));
        list.add(new Weather("overcast", "mild", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "normal", "low", "no"));
        list.add(new Weather("rain", "mild", "high", "high", "no"));

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static InstanceList getDataSameOutlook() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "no"));
        list.add(new Weather("sun", "hot", "high", "high", "no"));
        list.add(new Weather("sun", "mild", "high", "low", "no"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static InstanceList getDataPlayAllNo() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "no"));
        list.add(new Weather("sun", "hot", "high", "high", "no"));
        list.add(new Weather("rain", "cold", "normal", "high", "no"));
        list.add(new Weather("sun", "mild", "high", "low", "no"));
        list.add(new Weather("rain", "mild", "high", "high", "no"));

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static InstanceList getDataPlayAllYes() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("overcast", "hot", "high", "low", "yes"));
        list.add(new Weather("rain", "mild", "high", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "low", "yes"));
        list.add(new Weather("overcast", "cold", "normal", "high", "yes"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "normal", "low", "yes"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));
        list.add(new Weather("overcast", "mild", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "normal", "low", "yes"));

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    public static InstanceList getDataNoOvercast() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "yes"));
        list.add(new Weather("sun", "hot", "high", "high", "no"));
        list.add(new Weather("rain", "mild", "high", "low", "no"));
        list.add(new Weather("rain", "cold", "normal", "low", "no"));
        list.add(new Weather("rain", "cold", "normal", "high", "no"));
        list.add(new Weather("sun", "mild", "high", "low", "yes"));
        list.add(new Weather("sun", "cold", "normal", "low", "yes"));
        list.add(new Weather("rain", "mild", "normal", "low", "no"));
        list.add(new Weather("sun", "mild", "normal", "high", "yes"));
        list.add(new Weather("sun", "mild", "high", "high", "no") );

        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }
}
