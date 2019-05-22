package com.example.nfc_call_taxi;

public class Data_Taxi {
    String taxi_name,taxi_number,taxi_phonenumber;
    Data_Taxi(){}
    Data_Taxi(String taxi_name,String taxi_number,String taxi_phonenumber){
        this.taxi_name = taxi_name;
        this.taxi_number = taxi_number;
        this.taxi_phonenumber = taxi_phonenumber;
    }

    public String getTaxi_name() {
        return taxi_name;
    }

    public String getTaxi_number() {
        return taxi_number;
    }

    public String getTaxi_phonenumber() {
        return taxi_phonenumber;
    }

    public void setTaxi_name(String taxi_name) {
        this.taxi_name = taxi_name;
    }

    public void setTaxi_number(String taxi_number) {
        this.taxi_number = taxi_number;
    }

    public void setTaxi_phonenumber(String taxi_phonenumber) {
        this.taxi_phonenumber = taxi_phonenumber;
    }
}
