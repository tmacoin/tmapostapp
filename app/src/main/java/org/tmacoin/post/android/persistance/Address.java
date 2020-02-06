package org.tmacoin.post.android.persistance;

public class Address {

    private String tmaAddress;
    private String name;

    public Address(String tmaAddress, String name) {
        this.tmaAddress = tmaAddress;
        this.name = name;
    }

    public String getTmaAddress() {
        return tmaAddress;
    }

    public String getName() {
        return name;
    }

}
