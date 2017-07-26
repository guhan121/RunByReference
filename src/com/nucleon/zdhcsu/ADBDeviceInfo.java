package com.nucleon.zdhcsu;

public class ADBDeviceInfo {

    // List of devices attached
    // 0715f7b4f8730c34 device product:zenltezc model:SM_G9280 device:zenltechn

    String id = "";
    boolean online = false;
    String product = "";
    String model = "";
    String device = "";
    String brand = "";

    public ADBDeviceInfo(String in) {
        String[] strings = in.split(" +|\t");

        id = strings[0];
        if (strings[1].equals("device"))
            online = true;
        for (int i = 2; i < strings.length; i++) {
            if (strings[i].contains("device:"))
                device = strings[i].substring("device:".length(), strings[i].length());
            else if (strings[i].contains("product:"))
                product = strings[i].substring("product:".length(), strings[i].length());
            else if (strings[i].contains("model:"))
                model = strings[i].substring("model:".length(), strings[i].length());
        }

    }

    public String getSimpleInfo() {
        return model + " : " + id;
    }

    public String toString() {
        return model + " : " + id;
    }

}
