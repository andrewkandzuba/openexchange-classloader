package io.openexchange.model;

public class House {

    private final String address;
    private final Person owner;

    public House(String address, Person owner) {
        this.address = address;
        this.owner = owner;
    }

    public String getAddress() {
        return address;
    }

    public Person getOwner() {
        return owner;
    }
}
