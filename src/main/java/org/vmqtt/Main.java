package org.vmqtt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Main
{
    public static String clientId;
    
    public static String serialNumber;

    public static String username;
    public static String password;

    public static String mqttBrokerHost;
    public static int mqttBrokerPort = 1883;

    public static int interval = 5;
    public static String token;

    public static HttpClient client;

    public static void main(String[] args)
    {
        Options options = new Options();

        Option serialNumberOpt = new Option("serialNumber", true, "Serial number of the vaillant heating/cooling device");
        serialNumberOpt.setRequired(true);
        options.addOption(serialNumberOpt);

        Option mqttBrokerHostOpt = new Option("mqttBrokerHost", true, "MQTT broker hostname/ip");
        mqttBrokerHostOpt.setRequired(true);
        options.addOption(mqttBrokerHostOpt);
        Option mqttBrokerPortOpt = new Option("mqttBrokerPort", true, "MQTT broker port number. default: 1883");
        mqttBrokerPortOpt.setRequired(false);
        options.addOption(mqttBrokerPortOpt);

        Option usernameOpt = new Option("username", true, "Vaillant username");
        usernameOpt.setRequired(true);
        options.addOption(usernameOpt);
        Option passwordOpt = new Option("password", true, "Vaillant password");
        passwordOpt.setRequired(true);
        options.addOption(passwordOpt);

        Option intervalOpt = new Option("interval", true, "Publish interval in minutes. default: 5 minutes");
        intervalOpt.setRequired(false);
        options.addOption(intervalOpt);

        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose

        String userDir = System.getProperty("user.dir");
        Path path = Paths.get(userDir);
        String project = "programm";

        if ( path.getFileName() != null )
            project = path.getFileName().toString();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(project+" [options]", options);

            System.exit(1);
        }

        System.out.println("Application started");

        serialNumber = cmd.getOptionValue(serialNumberOpt.getOpt());
        mqttBrokerHost = cmd.getOptionValue(mqttBrokerHostOpt.getOpt());
        username = cmd.getOptionValue(usernameOpt.getOpt());
        password = cmd.getOptionValue(passwordOpt.getOpt());

        //optional
        if ( cmd.getOptionValue(mqttBrokerPortOpt.getOpt()) != null )
            mqttBrokerPort = Integer.parseInt(cmd.getOptionValue(mqttBrokerPortOpt.getOpt()));
        if ( cmd.getOptionValue(intervalOpt.getOpt()) != null )
            interval = Integer.parseInt(cmd.getOptionValue(intervalOpt.getOpt()));

        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        clientId = getClientId();

        System.out.println("Serialnumber: "+serialNumber);
        System.out.println("MqttBrokerHost: "+mqttBrokerHost);
        System.out.println("MqttBrokerPort: "+mqttBrokerPort);
        System.out.println("Username: "+username);
        System.out.println("Password: *********");
        System.out.println("Interval: "+interval+" minutes");
        System.out.println("client id: "+clientId);

        while (true)
        {
            try
            {
                worker();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                Thread.sleep(interval*60*1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String getClientId()
    {
        int length = 202;
        String result = "";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int charactersLength = characters.length();

        for (int i = 0; i < length; i++)
        {
            result += characters.charAt((int) Math.floor(Math.random() * charactersLength));
        }

        return "multimatic_" + result;
    }

    public static String getToken() throws IOException, InterruptedException
    {
        String body = "{ "+
            "\"smartphoneId\":\""+clientId+"\", "+
            "\"password\": \""+password+"\", "+
            "\"username\": \""+username+"\" }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/account/authentication/v1/token/new"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        LinkedTreeMap map = gson.fromJson(response.body(), LinkedTreeMap.class);

        return (((LinkedTreeMap) map.get("body")).get("authToken")).toString();
    }

    public static boolean login() throws IOException, InterruptedException
    {
        String body = "{ "+
                "\"smartphoneId\":\""+clientId+"\", "+
                "\"authToken\": \""+token+"\", "+
                "\"username\": \""+username+"\" }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/account/authentication/v1/authenticate"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return (response.statusCode()==200);
    }

    public static boolean logout() throws IOException, InterruptedException
    {
        String body = "{ "+
                "\"smartphoneId\":\""+clientId+"\", "+
                "\"authToken\": \""+token+"\", "+
                "\"username\": \""+username+"\" }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/account/authentication/v1/logout"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return (response.statusCode()==200);
    }

    public static String getOutsideTempJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/systemcontrol/v1/status"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonObject().toString();
    }

    public static String getSystemStatusJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/system/v1/status"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();



        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonObject().toString();
    }

    public static String getSystemDetailsJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/system/v1/details"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();



        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonObject().toString();
    }

    public static String getDhwJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/systemcontrol/v1/dhw"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();



        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonArray().get(0).getAsJsonObject().toString();
    }

    public static String getEmfJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/emf/v1/devices"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonArray().get(0).getAsJsonObject().toString();
    }

    public static String getZoneStateJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/systemcontrol/v1/zones"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();



        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        return map.get("body").getAsJsonArray().get(0).getAsJsonObject().toString();
    }

    public static String getLiveReportJson() throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://smart.vaillant.com/mobile/api/v4/facilities/"+serialNumber+"/livereport/v1"))
                .header("User-Agent", "okhttp/3.10.0")
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();



        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();

        var map = gson.fromJson(response.body(), JsonObject.class);

        JsonObject reports = new JsonObject();

        for ( var x : map.get("body").getAsJsonObject().get("devices").getAsJsonArray() )
        {
            var rep = x.getAsJsonObject().get("reports").getAsJsonArray().get(0);

            reports.add(rep.getAsJsonObject().get("_id").toString().replace("\"", ""), rep);
        }
            //System.out.println(x);

        return reports.toString();
    }

    public static void worker() throws IOException, InterruptedException, MqttException
    {
        token = getToken();

        System.out.println("Authtoken: "+token);

        login();

        Gson gson = new Gson();

        String publisherId = UUID.randomUUID().toString();
        IMqttClient publisher = new MqttClient("tcp://"+mqttBrokerHost+":"+mqttBrokerPort+"", publisherId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);

        //mqtt publish
        try {String json = getSystemStatusJson(); publishMessage("vaillant/systemState", json, publisher);} catch (Exception e) { e.printStackTrace(); };
        try {String json = getSystemDetailsJson(); publishMessage("vaillant/systemDetails", json, publisher);} catch (Exception e) { e.printStackTrace(); };
        try {String json = getDhwJson(); publishMessage("vaillant/dhwState", json, publisher);} catch (Exception e) { e.printStackTrace(); };
        try {String json = getEmfJson(); publishMessage("vaillant/emfState", json, publisher);} catch (Exception e) { e.printStackTrace(); };
        try {String json = getZoneStateJson(); publishMessage("vaillant/zoneState", json, publisher);} catch (Exception e) { e.printStackTrace(); };
        try {String json = getLiveReportJson(); publishMessage("vaillant/liveReport", json, publisher);} catch (Exception e) { e.printStackTrace(); };

        publisher.disconnect();

        logout();
    }

    public static void publishMessage(String topic, String payloadStr, IMqttClient publisher) throws MqttException
    {
        MqttMessage payload = new MqttMessage(payloadStr.getBytes("UTF8"));
        payload.setQos(0);
        payload.setRetained(true);
        publisher.publish(topic ,payload);
    }
}
